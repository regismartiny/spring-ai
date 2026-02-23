package com.example.demo.tools.weather.model.weatherapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Alert(@JsonProperty("features") List<Feature> features) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(@JsonProperty("properties") Properties properties) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(@JsonProperty("event") String event, @JsonProperty("areaDesc") String areaDesc,
                             @JsonProperty("severity") String severity, @JsonProperty("description") String description,
                             @JsonProperty("instruction") String instruction) {
    }
}
