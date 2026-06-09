package com.example.ai.demo.controller;

import java.util.List;

import com.example.ai.demo.model.ChatMessageResponse;
import com.example.ai.demo.model.ChatRequest;
import com.example.ai.demo.model.ChatResponse;
import com.example.ai.demo.model.RagSearchResult;
import com.example.ai.demo.service.ChatHistoryService;
import com.example.ai.demo.service.NotionMcpChatService;
import com.example.ai.demo.service.OllamaChatService;
import com.example.ai.demo.service.OpenAiChatService;
import com.example.ai.demo.service.RagService;

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

    private final OpenAiChatService openAiChatService;
    private final OllamaChatService ollamaChatService;
    private final NotionMcpChatService notionMcpChatService;
    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;

    public ChatClientController(OpenAiChatService openAiChatService,
                                OllamaChatService ollamaChatService,
                                NotionMcpChatService notionMcpChatService,
                                RagService ragService,
                                ChatHistoryService chatHistoryService) {
        this.openAiChatService = openAiChatService;
        this.ollamaChatService = ollamaChatService;
        this.notionMcpChatService = notionMcpChatService;
        this.ragService = ragService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping({"/api/chatclient/openai/chat", "/api/chat"})
    public ChatResponse openAiChat(@RequestBody ChatRequest request) {
        return new ChatResponse(openAiChatService.chat(request.message()));
    }

    @GetMapping({"/api/chatclient/openai/chat", "/api/chat"})
    public ChatResponse openAiChatByQuery(@RequestParam("q") String q) {
        return new ChatResponse(openAiChatService.chat(q));
    }

    @PostMapping("/api/chatclient/openai/notion/chat")
    public ChatResponse notionMcpChat(@RequestBody ChatRequest request) {
        return new ChatResponse(notionMcpChatService.chat(request.message()));
    }

    @GetMapping("/api/chatclient/openai/notion/chat")
    public ChatResponse notionMcpChatByQuery(@RequestParam("q") String q) {
        return new ChatResponse(notionMcpChatService.chat(q));
    }

    @GetMapping("/api/notion/mcp/tools")
    public List<String> notionMcpTools() {
        return notionMcpChatService.tools();
    }

    @GetMapping("/api/notion/mcp/search")
    public String notionMcpSearch(@RequestParam(value = "q", defaultValue = "") String q) {
        return notionMcpChatService.search(q);
    }

    @PostMapping({"/api/chatclient/ollama/chat", "/api/chat/ollama"})
    public ChatResponse ollamaChat(@RequestBody ChatRequest request) {
        return new ChatResponse(ollamaChatService.chat(request.message()));
    }

    @GetMapping({"/api/chatclient/ollama/chat", "/api/chat/ollama"})
    public ChatResponse ollamaChatByQuery(@RequestParam("q") String q) {
        return new ChatResponse(ollamaChatService.chat(q));
    }

    @GetMapping({"/api/chatclient/openai/chat/memory", "/api/chat/memory"})
    public String openAiMemoryChat(@RequestParam("id") String id, @RequestParam("q") String q){
        return openAiChatService.chatWithMemory(q, id);
    }

    @GetMapping("/api/chatclient/ollama/chat/memory")
    public String ollamaMemoryChat(@RequestParam("id") String id, @RequestParam("q") String q){
        return ollamaChatService.chatWithMemory(q, id);
    }

    @GetMapping({"/api/chatclient/openai/movie/recommend", "/movie/recommend"})
    public ChatResponse recommendMovies(@RequestParam("q") String q) {
        return new ChatResponse(openAiChatService.recommendMovies(q));
    }

    @GetMapping(value = {"/api/chatclient/openai/movie/stream", "/movie/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestParam("q") String q) {
        return openAiChatService.streamRecommendMovies(q)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value={"/api/chatclient/openai/chat/stream", "/chat/stream"}, produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> openAiMemoryStream(@RequestParam("id") String id,
                                                            @RequestParam("q") String q){

        return openAiChatService.streamChatWithMemory(q, id)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value={"/api/chatclient/ollama/chat/stream", "/chat/ollama/stream"}, produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> ollamaStream(@RequestParam("q") String q){
        return ollamaChatService.streamChat(q)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping(value="/api/chatclient/ollama/chat/memory/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> ollamaMemoryStream(@RequestParam("id") String id,
                                                            @RequestParam("q") String q){
        return ollamaChatService.streamChatWithMemory(q, id)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    @GetMapping({"/api/chatclient/openai/chat/history", "/chat/history"})
    public List<ChatMessageResponse> history(@RequestParam("id") String conversationId) {
        return chatHistoryService.getHistory(conversationId);
    }

    @GetMapping("/api/rag/search")
    public List<RagSearchResult> searchRagDocuments(@RequestParam("q") String q) {
        return ragService.searchResults(q);
    }
}
