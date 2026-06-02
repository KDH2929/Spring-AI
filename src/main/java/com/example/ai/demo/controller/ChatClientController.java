package com.example.ai.demo.controller;

import java.util.List;

import com.example.ai.demo.service.ChatClientService;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class ChatClientController {

    private final ChatClientService chatClientService;
    private final ChatMemory chatMemory;

    public ChatClientController(ChatClientService chatClientService, ChatMemory chatMemory) {
        this.chatClientService = chatClientService;
        this.chatMemory = chatMemory;
    }

    @PostMapping({"/api/chatclient/openai/chat", "/api/chat"})
    public ChatResponse openAiChat(@RequestBody ChatRequest request) {
        return new ChatResponse(chatClientService.chatWithOpenAi(request.message()));
    }

    @GetMapping({"/api/chatclient/openai/chat", "/api/chat"})
    public ChatResponse openAiChatByQuery(@RequestParam("q") String q) {
        return new ChatResponse(chatClientService.chatWithOpenAi(q));
    }

    @PostMapping({"/api/chatclient/ollama/chat", "/api/chat/ollama"})
    public ChatResponse ollamaChat(@RequestBody ChatRequest request) {
        return new ChatResponse(chatClientService.chatWithOllama(request.message()));
    }

    @GetMapping({"/api/chatclient/ollama/chat", "/api/chat/ollama"})
    public ChatResponse ollamaChatByQuery(@RequestParam("q") String q) {
        return new ChatResponse(chatClientService.chatWithOllama(q));
    }

    @GetMapping({"/api/chatclient/openai/chat/memory", "/api/chat/memory"})
    public String openAiMemoryChat(@RequestParam("id") String id, @RequestParam("q") String q){
        return chatClientService.chatWithOpenAiMemory(q, id);
    }

    @GetMapping("/api/chatclient/ollama/chat/memory")
    public String ollamaMemoryChat(@RequestParam("id") String id, @RequestParam("q") String q){
        return chatClientService.chatWithOllamaMemory(q, id);
    }

    @GetMapping({"/api/chatclient/openai/movie/recommend", "/movie/recommend"})
    public ChatResponse recommendMovies(@RequestParam("q") String q) {
        return new ChatResponse(chatClientService.recommendMovies(q));
    }

    @GetMapping(value = {"/api/chatclient/openai/movie/stream", "/movie/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestParam("q") String q) {
        return chatClientService.streamRecommend(q)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value={"/api/chatclient/openai/chat/stream", "/chat/stream"}, produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> openAiMemoryStream(@RequestParam("id") String id,
                                                            @RequestParam("q") String q){

        return chatClientService.streamOpenAiMemoryChat(q, id)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value={"/api/chatclient/ollama/chat/stream", "/chat/ollama/stream"}, produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> ollamaStream(@RequestParam("q") String q){
        return chatClientService.streamOllamaChat(q)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value="/api/chatclient/ollama/chat/memory/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> ollamaMemoryStream(@RequestParam("id") String id,
                                                            @RequestParam("q") String q){
        return chatClientService.streamOllamaMemoryChat(q, id)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping({"/api/chatclient/openai/chat/history", "/chat/history"})
    public List<ChatMsgDto> history(@RequestParam("id") String conversationId) {
        return chatMemory.get(conversationId).stream()
                .map(msg -> new ChatMsgDto(msg.getMessageType().name(), msg.getText()))
                .toList();
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String content) {
    }

    public record ChatMsgDto(String type, String text) {
    }
}
