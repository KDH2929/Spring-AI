package com.example.ai.demo.service;

import java.util.List;

import com.example.ai.demo.model.ChatMessageResponse;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatHistoryService {

    private final ChatMemory chatMemory;

    public ChatHistoryService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public List<ChatMessageResponse> getHistory(String conversationId) {
        return chatMemory.get(conversationId).stream()
                .map(msg -> new ChatMessageResponse(msg.getMessageType().name(), msg.getText()))
                .toList();
    }
}
