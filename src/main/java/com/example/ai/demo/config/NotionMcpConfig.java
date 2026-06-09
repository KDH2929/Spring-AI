package com.example.ai.demo.config;

import java.net.http.HttpRequest;
import java.time.Duration;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotionMcpConfig {

    @Bean(destroyMethod = "closeGracefully")
    public McpSyncClient notionMcpClient(
            @Value("${notion.mcp.base-url}") String baseUrl,
            @Value("${notion.mcp.auth-token:}") String authToken) {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .header("Accept", "application/json, text/event-stream");

        if (authToken != null && !authToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        var transport = HttpClientStreamableHttpTransport.builder(baseUrl)
                .endpoint("/mcp")
                .requestBuilder(requestBuilder)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // For eager connection checks, create a client variable, call initialize(), then return it.
        return McpClient.sync(transport)
                .clientInfo(McpSchema.Implementation.builder("demo-notion-client", "0.0.1").build())
                .requestTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean(name = "notionMcpToolCallbacks")
    ToolCallbackProvider notionMcpToolCallbacks(McpSyncClient notionMcpClient) {
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(notionMcpClient)
                .build();
    }
}
