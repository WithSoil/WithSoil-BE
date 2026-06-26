package com.outthedoor.withsoil.api.weather.dto.response;

import java.time.OffsetDateTime;

public record WeatherForecastResponse(
        OffsetDateTime announcedAt,
        OffsetDateTime forecastAt,
        double latitude,
        double longitude,
        int gridX,
        int gridY,
        Double temperature,
        String sky,
        String precipitationType,
        Integer precipitationProbability,
        Double hourlyPrecipitation,
        Integer humidity,
        Double windSpeed,
        String message
) {
}
