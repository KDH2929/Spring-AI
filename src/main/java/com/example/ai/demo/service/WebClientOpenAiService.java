package com.example.ai.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class WebClientOpenAiService {

    private final WebClient openAiWebClient;

    public WebClientOpenAiService(@Qualifier("openAiWebClient") WebClient openAiWebClient) {
        this.openAiWebClient = openAiWebClient;
    }

    public Mono<String> requestChatCompletion(String userInput) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-5-nano",
                "messages", new Object[] {
                        Map.of("role", "user", "content", userInput)
                }
        );

        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }
}
