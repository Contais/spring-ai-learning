package com.learn.ai.service;

import com.learn.ai.entity.ChatConversation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatOrchestratorService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatConversationService conversationService;

    @Autowired
    private ChatMessageService messageService;

    @Autowired
    private ChatMemory chatMemory;


    /**
     * 流式发送消息
     */
    public Flux<String> streamMessage(Long conversationId, String userMessage) {
        // 1. 确保会话存在
        if (conversationId == null) {
            // 处理 conversationId 为 null 的情况（首次对话）
            ChatConversation newConversation = conversationService.createConversation("新对话", "chat");
            conversationId = newConversation.getId();
        } else {
            // 检查会话是否存在
            ChatConversation conversation = conversationService.getById(conversationId);
            if (conversation == null) {
                // 会话不存在，创建新会话
                ChatConversation newConversation = conversationService.createConversation("新对话", "chat");
                conversationId = newConversation.getId();
            }
        }
        // 使用 final 变量供 lambda 表达式使用
        final Long finalConversationId = conversationId;

        // 2. 保存用户消息
        messageService.saveUserMessage(conversationId, userMessage);

        // 3. 更新会话时间
        conversationService.touchConversation(conversationId);

        // 4. 流式调用 AI
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .user(userMessage)
                .advisors(advice -> advice.param(
                        ChatMemory.CONVERSATION_ID,
                        finalConversationId.toString()
                ))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流完成后，保存完整的 AI 回复
                    String aiResponse = fullResponse.toString();
                    messageService.saveAssistantMessage(finalConversationId, aiResponse);

                    // 如果是第一次对话，生成标题
                    if (messageService.countBySessionId(finalConversationId) == 2) {
                        String autoTitle = generateTitle(userMessage);
                        conversationService.updateConversationTitle(finalConversationId, autoTitle);
                    }

                    // 再次更新时间
                    conversationService.touchConversation(finalConversationId);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long conversationId) {
        messageService.removeBySessionId(conversationId);
        conversationService.removeById(conversationId);
        chatMemory.clear(conversationId.toString());
    }

    private String generateTitle(String firstMessage) {
        if (firstMessage == null || firstMessage.isEmpty()) {
            return "新对话";
        }
        return firstMessage.length() > 20
                ? firstMessage.substring(0, 20) + "..."
                : firstMessage;
    }
}
