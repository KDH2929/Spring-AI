package com.example.ai.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ai.demo.tools.WeatherTool;

@Configuration
public class ChatClientOpenAiOllamaConfig {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel, WeatherTool weatherTool) {
        return ChatClient.builder(chatModel)
                .defaultTools(weatherTool)
                .build();
    }

    @Bean
    public ChatClient notionOpenAiChatClient(
            OpenAiChatModel chatModel,
            @Qualifier("notionMcpToolCallbacks") ToolCallbackProvider notionMcpToolCallbacks) {

        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        You can use Notion MCP tools when the user's request requires reading or updating Notion.
                        Use the tools only when they are relevant to the user's request.
                        """)
                .defaultToolCallbacks(notionMcpToolCallbacks.getToolCallbacks())
                .build();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }


}
