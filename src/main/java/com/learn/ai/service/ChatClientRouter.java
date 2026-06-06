package com.learn.ai.service;

import com.learn.ai.enums.ChatScene;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatClientRouter {

    private final Map<String, ChatClient> chatClientMap;
    private final Map<String, ChatMemory> chatMemoryMap;

    public ChatClient getClient(ChatScene scene) {
        ChatClient chatClient = chatClientMap.get(scene.getClientBeanName());
        if (chatClient == null) {
            throw new IllegalArgumentException("未找到对应的 ChatClient: " + scene.name());
        }
        return chatClient;
    }

    public ChatMemory getChatMemory(ChatScene scene) {
        ChatMemory chatMemory = chatMemoryMap.get(scene.getChatMemoryBeanName());
        if (chatMemory == null) {
            throw new IllegalArgumentException("未找到对应的 ChatMemory: " + scene.name());
        }
        return chatMemory;
    }
}
