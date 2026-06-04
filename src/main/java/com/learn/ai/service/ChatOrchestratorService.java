
package com.learn.ai.service;

import com.learn.ai.entity.ChatSession;
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
    private ChatSessionService sessionService;

    @Autowired
    private ChatMessageService messageService;

    @Autowired
    private ChatMemory chatMemory;


    /**
     * 流式发送消息
     */
    public Flux<String> streamMessage(String sessionId, String userMessage) {
        // 1. 确保会话存在
        ChatSession session = sessionService.getById(sessionId);
        if (session == null) {
            session = sessionService.createSession("新对话", "chat");
            sessionId = session.getId();
        }
        // 使用 final 变量供 lambda 表达式使用
        final String finalSessionId = sessionId;

        // 2. 保存用户消息
        messageService.saveUserMessage(sessionId, userMessage);

        // 3. 更新会话时间
        sessionService.touchSession(sessionId);

        // 4. 流式调用 AI
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .user(userMessage)
                .advisors(advice -> advice.param(
                        ChatMemory.CONVERSATION_ID,
                        finalSessionId
                ))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流完成后，保存完整的 AI 回复
                    String aiResponse = fullResponse.toString();
                    messageService.saveAssistantMessage(finalSessionId, aiResponse);

                    // 如果是第一次对话，生成标题
                    if (messageService.countBySessionId(finalSessionId) == 2) {
                        String autoTitle = generateTitle(userMessage);
                        sessionService.updateSessionTitle(finalSessionId, autoTitle);
                    }

                    // 再次更新时间
                    sessionService.touchSession(finalSessionId);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(String sessionId) {
        messageService.removeBySessionId(sessionId);
        sessionService.removeById(sessionId);
        chatMemory.clear(sessionId);
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