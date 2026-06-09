package com.example.ai.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class OllamaChatService {

    private final ChatClient ollamaChatClient;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final RagService ragService;

    public OllamaChatService(@Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                                        ChatMemory chatClientMemory,
                                        RagService ragService) {

        this.ollamaChatClient = ollamaChatClient;
        this.chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatClientMemory).build();
        this.ragService = ragService;
        
    }

    public String chat(String userInput) {
        return ollamaChatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    public Flux<String> streamChat(String userInput) {
        return ollamaChatClient.prompt()
                .user(userInput)
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    public String chatWithMemory(String userInput, String conversationId) {
        return ollamaChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public Flux<String> streamChatWithMemory(String userInput, String conversationId) {
        var prompt = ollamaChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversationId));

        if (ragService.hasRelevantDocs(userInput)) {
            prompt.advisors(ragService.advisor());
        }

        return prompt
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    private String preserveSseLeadingSpace(String chunk) {
        if (chunk == null) {
            return "";
        }
        return chunk.startsWith(" ") ? " " + chunk : chunk;
    }
}
