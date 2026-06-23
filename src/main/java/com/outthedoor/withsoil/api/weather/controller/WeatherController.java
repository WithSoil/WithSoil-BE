package com.outthedoor.withsoil.api.weather.controller;

import com.outthedoor.withsoil.api.weather.dto.response.WeatherForecastResponse;
import com.outthedoor.withsoil.api.weather.service.WeatherService;
import com.outthedoor.withsoil.api.weather.service.WeatherNotificationScheduler;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "기상(Weather)", description = "기상청 단기예보 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherNotificationScheduler weatherNotificationScheduler;

    @Operation(summary = "6시간 후 예보 조회", description = "현재 위치를 기준으로 약 6시간 후의 단기예보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/forecast/6-hours")
    public ResponseEntity<ApiResponse<WeatherForecastResponse>> getSixHourForecast(
            @RequestParam @DecimalMin("33.0") @DecimalMax("43.0") double latitude,
            @RequestParam @DecimalMin("124.0") @DecimalMax("132.0") double longitude
    ) {
        WeatherForecastResponse response = weatherService.getSixHourForecast(latitude, longitude);
        return ApiResponse.success(SuccessStatus.SUCCESS_WEATHER_FORECAST_GET, response);
    }

    @Operation(summary = "기상 푸시 테스트", description = "로그인 회원에게 6시간 후 기상예보 푸시를 즉시 발송합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/notifications/test")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        weatherNotificationScheduler.sendTestNotification(member);
        return ApiResponse.successOnly(SuccessStatus.SUCCESS_WEATHER_NOTIFICATION_SEND);
    }
}
