package com.example.ai.demo.controller;

import com.example.ai.demo.service.WebClientOpenAiService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class WebClientOpenAiController {

    private final WebClientOpenAiService webClientOpenAiService;

    public WebClientOpenAiController(WebClientOpenAiService webClientOpenAiService) {
        this.webClientOpenAiService = webClientOpenAiService;
    }

    @GetMapping({"/api/webclient/openai/chat", "/chat/webclient"})
    public Mono<String> chat(@RequestParam("q") String q) {
        return webClientOpenAiService.requestChatCompletion(q);
    }
}
