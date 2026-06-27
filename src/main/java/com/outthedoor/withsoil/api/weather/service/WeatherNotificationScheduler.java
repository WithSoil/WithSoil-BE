package com.outthedoor.withsoil.api.weather.service;

import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.api.member.entity.MemberLocation;
import com.outthedoor.withsoil.api.member.repository.MemberRepository;
import com.outthedoor.withsoil.api.weather.dto.response.WeatherForecastResponse;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherNotificationScheduler {

    private final MemberRepository memberRepository;
    private final WeatherService weatherService;
    private final ExpoPushService expoPushService;

    @Scheduled(cron = "0 0 0,6,12,18 * * *", zone = "Asia/Seoul")
    public void sendRegularForecasts() {
        sendForecasts();
    }

    private void sendForecasts() {
        List<Member> members = memberRepository.findAllByIsDeletedFalseAndPushTokenIsNotNull();
        for (Member member : members) {
            try {
                sendToMember(member);
            } catch (Exception e) {
                log.warn("[WeatherNotification] 회원 예보 알림 처리 실패 - memberId={}", member.getId(), e);
            }
        }
    }

    public void sendTestNotification(Member member) {
        if (member.getPushToken() == null || member.getPushToken().isBlank()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_PUSH_TOKEN_REQUIRED);
        }
        MemberLocation location = member.getLocation();
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_LOCATION_REQUIRED);
        }
        sendToMember(member);
    }

    private void sendToMember(Member member) {
        MemberLocation location = member.getLocation();
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            return;
        }

        WeatherForecastResponse forecast = weatherService.getSixHourForecast(
                location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue()
        );
        expoPushService.send(
                member.getPushToken(),
                "6시간 후 농장 기상예보",
                forecast.message(),
                "REGULAR_FORECAST"
        );
    }
}
