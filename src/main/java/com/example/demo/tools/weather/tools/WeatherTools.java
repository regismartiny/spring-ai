package com.example.demo.tools.weather.tools;

import com.example.demo.tools.weather.adapter.WeatherAPIAdapter;
import com.example.demo.tools.weather.model.weatherapi.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WeatherTools {

    private final WeatherAPIAdapter weatherAPIAdapter;

    /**
     * Get forecast for a specific latitude/longitude
     * @param latitude Latitude
     * @param longitude Longitude
     * @return The forecast for the given location
     * @throws RestClientException if the request fails
     */
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {

        var forecast = weatherAPIAdapter.getWeatherForecastByLocation(latitude, longitude);

        return forecast.properties().periods().stream().map(p -> {
            return String.format("""
					%s:
					Temperature: %s %s
					Wind: %s %s
					Forecast: %s
					""", p.name(), p.temperature(), p.temperatureUnit(), p.windSpeed(), p.windDirection(),
                    p.detailedForecast());
        }).collect(Collectors.joining());
    }

    /**
     * Get alerts for a specific area
     * @param state Area code. Two-letter US state code (e.g. CA, NY)
     * @return Human-readable alert information
     * @throws RestClientException if the request fails
     */
    @Tool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
    public String getAlerts(@ToolParam( description =  "Two-letter US state code (e.g. CA, NY") String state) {
        Alert alert = weatherAPIAdapter.getAlerts(state);

        return alert.features()
                .stream()
                .map(f -> String.format("""
					Event: %s
					Area: %s
					Severity: %s
					Description: %s
					Instructions: %s
					""", f.properties().event(), f.properties().areaDesc(), f.properties().severity(),
                        f.properties().description(), f.properties().instruction()))
                .collect(Collectors.joining("\n"));
    }

}
