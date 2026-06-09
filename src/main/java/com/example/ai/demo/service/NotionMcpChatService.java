package com.example.ai.demo.service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class NotionMcpChatService {

    private final ChatClient notionOpenAiChatClient;
    private final ToolCallbackProvider notionMcpToolCallbacks;

    public NotionMcpChatService(@Qualifier("notionOpenAiChatClient") ChatClient notionOpenAiChatClient,
                                @Qualifier("notionMcpToolCallbacks") ToolCallbackProvider notionMcpToolCallbacks) {
        this.notionOpenAiChatClient = notionOpenAiChatClient;
        this.notionMcpToolCallbacks = notionMcpToolCallbacks;
    }

    public String chat(String userInput) {
        return notionOpenAiChatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    public List<String> tools() {
        return Arrays.stream(notionMcpToolCallbacks.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .toList();
    }

    public String search(String query) {
        ToolCallback searchTool = Arrays.stream(notionMcpToolCallbacks.getToolCallbacks())
                .filter(callback -> "API_post_search".equals(callback.getToolDefinition().name()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("API_post_search tool callback not found"));

        return searchTool.call("""
                {"query":"%s"}
                """.formatted(escapeJson(query == null ? "" : query)).trim());
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
