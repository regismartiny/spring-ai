package com.example.demo.tools.weather.adapter;

import com.example.demo.tools.weather.model.weatherapi.Alert;
import com.example.demo.tools.weather.model.weatherapi.Forecast;
import com.example.demo.tools.weather.model.weatherapi.Points;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.util.Objects.isNull;

@Component
public class WeatherAPIAdapter {

    private static final String BASE_URL = "https://api.weather.gov";
    private final RestClient restClient;

    public WeatherAPIAdapter(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    public Forecast getWeatherForecastByLocation(double latitude, double longitude) {

        var points = restClient.get()
                .uri("/points/{latitude},{longitude}", latitude, longitude)
                .retrieve()
                .body(Points.class);

        if (isNull(points)) {
            throw new RuntimeException("API returned no points");
        }

        return restClient.get().uri(points.properties().forecast()).retrieve().body(Forecast.class);
    }

    public Alert getAlerts(String state) {
        var alert = restClient.get().uri("/alerts/active/area/{state}", state).retrieve().body(Alert.class);
        if (isNull(alert)) {
            throw new RuntimeException("API returned no alerts");
        }
        return alert;
    }
}
