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
public class ChatClientService {

    private final String template = """
            당신은 전문 영화 평론가입니다.
            사용자가 입력한 정보를 기반으로 영화를 추천하세요.
            사용자 입력: {userInput}
            """;

    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;

    public ChatClientService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                             @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                             ChatMemory chatClientMemory) {
        this.openAiChatClient = openAiChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatClientMemory).build();
    }

    public String chatWithOpenAi(String q) {
        return openAiChatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    public String chatWithOllama(String q) {
        return ollamaChatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    public String chatWithOpenAiMemory(String userInput, String userId){
        return openAiChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();
    }

    public Flux<String> streamOpenAiMemoryChat(String userInput, String userId){
        return openAiChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, userId))
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    public Flux<String> streamOllamaChat(String userInput) {
        return ollamaChatClient.prompt()
                .user(userInput)
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    public String chatWithOllamaMemory(String userInput, String userId){
        return ollamaChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();
    }

    public Flux<String> streamOllamaMemoryChat(String userInput, String userId){
        return ollamaChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, userId))
                .stream()
                .content()
                .map(this::preserveSseLeadingSpace)
                .concatWith(Flux.just("[[END]]"));
    }

    public String recommendMovies(String userInput) {
        PromptTemplate template = new PromptTemplate(this.template);
        Prompt prompt = template.create(Map.of("userInput", userInput));
        return openAiChatClient.prompt(prompt)
                .call()
                .content();
    }

    public Flux<String> streamRecommend(String userInput) {
        PromptTemplate template = new PromptTemplate(this.template);
        Prompt prompt = template.create(Map.of("userInput", userInput));
        return openAiChatClient.prompt(prompt)
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
