package com.outthedoor.withsoil.api.weather.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherServiceTest {

    @Test
    void convertsLatitudeLongitudeToKmaGrid() {
        WeatherService.GridPoint seoul = WeatherService.toGrid(37.5665, 126.9780);

        assertThat(seoul.x()).isEqualTo(60);
        assertThat(seoul.y()).isEqualTo(127);
    }

    @Test
    void selectsLatestAvailableAnnouncement() {
        assertThat(WeatherService.latestAnnouncement(LocalDateTime.of(2026, 6, 22, 10, 10)))
                .isEqualTo(LocalDateTime.of(2026, 6, 22, 8, 0));
        assertThat(WeatherService.latestAnnouncement(LocalDateTime.of(2026, 6, 22, 1, 40)))
                .isEqualTo(LocalDateTime.of(2026, 6, 21, 23, 0));
    }

    @Test
    void readsRequestedCellFromGrid() {
        StringBuilder body = new StringBuilder();
        for (int i = 1; i <= WeatherService.GRID_WIDTH * WeatherService.GRID_HEIGHT; i++) {
            body.append(i).append(',');
        }

        assertThat(WeatherService.valueAt(body.toString(), 3, 1)).isEqualTo(3.0);
        assertThat(WeatherService.valueAt(body.toString(), 1, 2)).isEqualTo(150.0);
    }
}
