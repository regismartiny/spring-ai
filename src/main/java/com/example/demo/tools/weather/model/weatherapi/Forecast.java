package com.example.demo.tools.weather.model.weatherapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Forecast(@JsonProperty("properties") Props properties) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Props(@JsonProperty("periods") List<Period> periods) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Period(@JsonProperty("number") Integer number, @JsonProperty("name") String name,
                         @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
                         @JsonProperty("isDaytime") Boolean isDayTime, @JsonProperty("temperature") Integer temperature,
                         @JsonProperty("temperatureUnit") String temperatureUnit,
                         @JsonProperty("temperatureTrend") String temperatureTrend,
                         @JsonProperty("probabilityOfPrecipitation") Map probabilityOfPrecipitation,
                         @JsonProperty("windSpeed") String windSpeed, @JsonProperty("windDirection") String windDirection,
                         @JsonProperty("icon") String icon, @JsonProperty("shortForecast") String shortForecast,
                         @JsonProperty("detailedForecast") String detailedForecast) {
    }
}