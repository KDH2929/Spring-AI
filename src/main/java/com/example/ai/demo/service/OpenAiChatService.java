package com.example.ai.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class OpenAiChatService {

    private final String movieRecommendTemplate = """
            You are a professional movie recommender.
            Recommend movies based on the user's input.
            User input: {userInput}
            """;

    private final ChatClient openAiChatClient;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final RagService ragService;


    public OpenAiChatService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                             ChatMemory chatClientMemory,
                             RagService ragService) {
        this.openAiChatClient = openAiChatClient;
        this.chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatClientMemory).build();
        this.ragService = ragService;
    }

    public String chat(String userInput) {
        return openAiChatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    public String chatWithMemory(String userInput, String conversationId) {
        return openAiChatClient.prompt() 
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public Flux<String> streamChatWithMemory(String userInput, String conversationId) {
        var prompt = openAiChatClient.prompt()
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

    public String recommendMovies(String userInput) {
        Prompt prompt = moviePrompt(userInput);
        return openAiChatClient.prompt(prompt)
                .call()
                .content();
    }

    public Flux<String> streamRecommendMovies(String userInput) {
        Prompt prompt = moviePrompt(userInput);
        return openAiChatClient.prompt(prompt)
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    private Prompt moviePrompt(String userInput) {
        PromptTemplate template = new PromptTemplate(movieRecommendTemplate);
        return template.create(Map.of("userInput", userInput));
    }

    private String preserveSseLeadingSpace(String chunk) {
        if (chunk == null) {
            return "";
        }
        return chunk.startsWith(" ") ? " " + chunk : chunk;
    }

}
