package com.example.ai.demo.tools;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherTool {
    
    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(
        name = "getWeather",
        description = "Get the current weather for a specified Location."    
    )

    public WeatherResponse getWeather(@ToolParam(description = "The Location to get the weather for") String city) {
        String url = String.format(
                "%s?units=metric&appid=%s&q=%s",
                baseUrl,
                apiKey,
                city
        );

        Map<String, Object> data = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) data.get("weather");
        String weather = (String) weatherList.get(0).get("description");

        Map<String, Object> mainData = (Map<String, Object>) data.get("main");
        double temperature = ((Number) mainData.get("temp")).doubleValue();

        return new WeatherResponse(city, weather, temperature);
    }

    public record WeatherResponse(
        String city,
        String weather,
        double temperature
    ) {}
}
