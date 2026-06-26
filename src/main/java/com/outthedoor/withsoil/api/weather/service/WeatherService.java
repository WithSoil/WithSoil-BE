package com.outthedoor.withsoil.api.weather.service;

import com.outthedoor.withsoil.api.weather.dto.response.WeatherForecastResponse;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WeatherService {

    static final int GRID_WIDTH = 149;
    static final int GRID_HEIGHT = 253;
    private static final int[] BASE_HOURS = {2, 5, 8, 11, 14, 17, 20, 23};
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KMA_TIME = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private static final double MISSING_VALUE = -99.0;

    private final WebClient webClient;
    private final String authKey;
    private final Clock clock;
    private final Map<String, Mono<double[]>> gridCache = new ConcurrentHashMap<>();

    @Autowired
    public WeatherService(
            @Value("${kma.api.base-url}") String baseUrl,
            @Value("${kma.api.auth-key}") String authKey
    ) {
        this(WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build(), authKey, Clock.system(KST));
    }

    WeatherService(WebClient webClient, String authKey, Clock clock) {
        this.webClient = webClient;
        this.authKey = authKey;
        this.clock = clock;
    }

    public WeatherForecastResponse getSixHourForecast(double latitude, double longitude) {
        if (authKey == null || authKey.isBlank()) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_KMA_API);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime announcedAt = latestAnnouncement(now.minusMinutes(20));
        LocalDateTime forecastAt = now.plusHours(6).withMinute(0).withSecond(0).withNano(0);
        GridPoint grid = toGrid(latitude, longitude);

        try {
            Map<String, Double> values = Mono.zip(
                            fetch("TMP", announcedAt, forecastAt, grid),
                            fetch("SKY", announcedAt, forecastAt, grid),
                            fetch("PTY", announcedAt, forecastAt, grid),
                            fetch("POP", announcedAt, forecastAt, grid),
                            fetch("PCP", announcedAt, forecastAt, grid),
                            fetch("REH", announcedAt, forecastAt, grid),
                            fetch("WSD", announcedAt, forecastAt, grid)
                    )
                    .map(tuple -> Map.of(
                            "TMP", tuple.getT1(), "SKY", tuple.getT2(), "PTY", tuple.getT3(),
                            "POP", tuple.getT4(), "PCP", tuple.getT5(), "REH", tuple.getT6(),
                            "WSD", tuple.getT7()
                    ))
                    .block();

            if (values == null) {
                throw new IllegalStateException("Empty KMA response");
            }
            return createResponse(latitude, longitude, grid, announcedAt, forecastAt, values);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_KMA_API, e);
        }
    }

    private Mono<Double> fetch(String variable, LocalDateTime announcedAt,
                               LocalDateTime forecastAt, GridPoint grid) {
        String cacheKey = variable + ':' + announcedAt.format(KMA_TIME) + ':' + forecastAt.format(KMA_TIME);
        if (gridCache.size() > 64) {
            gridCache.clear();
        }

        Mono<double[]> gridMono = gridCache.computeIfAbsent(cacheKey, ignored -> webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/typ01/cgi-bin/url/nph-dfs_shrt_grd")
                        .queryParam("tmfc", announcedAt.format(KMA_TIME))
                        .queryParam("tmef", forecastAt.format(KMA_TIME))
                        .queryParam("vars", variable)
                        .queryParam("authKey", authKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .map(WeatherService::parseGrid)
                .doOnError(error -> gridCache.remove(cacheKey))
                .cache(Duration.ofHours(3)));

        return gridMono.map(values -> valueAt(values, grid.x(), grid.y()));
    }

    static LocalDateTime latestAnnouncement(LocalDateTime availableAt) {
        for (int i = BASE_HOURS.length - 1; i >= 0; i--) {
            if (availableAt.getHour() >= BASE_HOURS[i]) {
                return availableAt.toLocalDate().atTime(BASE_HOURS[i], 0);
            }
        }
        return availableAt.toLocalDate().minusDays(1).atTime(23, 0);
    }

    static double valueAt(String body, int gridX, int gridY) {
        return valueAt(parseGrid(body), gridX, gridY);
    }

    static double[] parseGrid(String body) {
        Matcher matcher = NUMBER.matcher(body);
        double[] values = new double[GRID_WIDTH * GRID_HEIGHT];
        int index = 0;
        while (matcher.find()) {
            if (index >= values.length) {
                throw new IllegalArgumentException("Invalid KMA grid response");
            }
            values[index++] = Double.parseDouble(matcher.group());
        }
        if (index != values.length) {
            throw new IllegalArgumentException("Invalid KMA grid response");
        }
        return values;
    }

    static double valueAt(double[] values, int gridX, int gridY) {
        int targetIndex = (gridY - 1) * GRID_WIDTH + (gridX - 1);
        double value = values[targetIndex];
        if (Double.compare(value, MISSING_VALUE) == 0) {
            throw new IllegalArgumentException("No forecast at grid " + gridX + "," + gridY);
        }
        return value;
    }

    static GridPoint toGrid(double latitude, double longitude) {
        double re = 6371.00877 / 5.0;
        double slat1 = Math.toRadians(30.0);
        double slat2 = Math.toRadians(60.0);
        double olon = Math.toRadians(126.0);
        double olat = Math.toRadians(38.0);
        double sn = Math.log(Math.cos(slat1) / Math.cos(slat2))
                / Math.log(Math.tan(Math.PI * 0.25 + slat2 * 0.5)
                / Math.tan(Math.PI * 0.25 + slat1 * 0.5));
        double sf = Math.pow(Math.tan(Math.PI * 0.25 + slat1 * 0.5), sn)
                * Math.cos(slat1) / sn;
        double ro = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + olat * 0.5), sn);
        double ra = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + Math.toRadians(latitude) * 0.5), sn);
        double theta = Math.toRadians(longitude) - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int x = (int) Math.floor(ra * Math.sin(theta) + 43.0 + 0.5);
        int y = (int) Math.floor(ro - ra * Math.cos(theta) + 136.0 + 0.5);
        if (x < 1 || x > GRID_WIDTH || y < 1 || y > GRID_HEIGHT) {
            throw new IllegalArgumentException("Location is outside KMA grid");
        }
        return new GridPoint(x, y);
    }

    private WeatherForecastResponse createResponse(
            double latitude, double longitude, GridPoint grid,
            LocalDateTime announcedAt, LocalDateTime forecastAt, Map<String, Double> values
    ) {
        double temperature = values.get("TMP");
        int skyCode = values.get("SKY").intValue();
        int ptyCode = values.get("PTY").intValue();
        int pop = values.get("POP").intValue();
        String sky = skyDescription(skyCode);
        String precipitation = precipitationDescription(ptyCode);
        String message = buildMessage(temperature, sky, precipitation, pop);
        ZoneOffset offset = ZoneOffset.ofHours(9);

        return new WeatherForecastResponse(
                announcedAt.atOffset(offset), forecastAt.atOffset(offset), latitude, longitude,
                grid.x(), grid.y(), temperature, sky, precipitation, pop,
                values.get("PCP"), values.get("REH").intValue(), values.get("WSD"), message
        );
    }

    static String skyDescription(int code) {
        return switch (code) {
            case 1 -> "맑음";
            case 3 -> "구름많음";
            case 4 -> "흐림";
            default -> "알 수 없음";
        };
    }

    static String precipitationDescription(int code) {
        return switch (code) {
            case 0 -> "없음";
            case 1 -> "비";
            case 2 -> "비/눈";
            case 3 -> "눈";
            case 4 -> "소나기";
            default -> "알 수 없음";
        };
    }

    static String buildMessage(double temperature, String sky, String precipitation, int pop) {
        String weather = "없음".equals(precipitation) ? sky : precipitation;
        return String.format(Locale.KOREAN, "6시간 후에는 %s, 기온은 %.0f°C로 예상됩니다. 강수확률은 %d%%입니다.",
                weather, temperature, pop);
    }

    record GridPoint(int x, int y) {
    }
}
