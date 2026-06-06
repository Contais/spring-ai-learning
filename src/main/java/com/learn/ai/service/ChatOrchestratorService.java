package com.learn.ai.service;

import com.learn.ai.entity.ChatConversation;
import com.learn.ai.enums.ChatScene;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatOrchestratorService {

    private final ChatClientRouter chatClientRouter;
    private final ChatConversationService conversationService;
    private final ChatMessageService messageService;


    /**
     * 流式发送消息
     */
    public Flux<String> streamMessage(ChatScene scene, Long conversationId, String userMessage) {
        // 1. 参数校验与路由
        ChatScene resolvedScene = Objects.requireNonNull(scene, "scene must not be null");
        String prompt = Objects.requireNonNull(userMessage, "userMessage must not be null");
        ChatClient chatClient = chatClientRouter.getClient(resolvedScene);

        // 2. 持久化场景：确保 DB 会话存在
        if (resolvedScene.isPersistMessages()) {
            conversationId = ensureConversation(resolvedScene, conversationId);
        }

        // 使用 final 变量供 lambda 表达式使用
        final Long finalConversationId = conversationId;
        final boolean persist = resolvedScene.isPersistMessages();

        // 2. 持久化场景：保存用户消息并更新会话时间
        if (persist) {
            messageService.saveUserMessage(conversationId, userMessage);
            conversationService.touchConversation(conversationId);
        }

        // 3. 流式调用 AI
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .user(prompt)
                .advisors(advice -> advice.param(
                        ChatMemory.CONVERSATION_ID,
                        finalConversationId.toString()
                ))
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    // 持久化场景：保存 AI 回复并更新会话
                    if (persist) {
                        String aiResponse = fullResponse.toString();
                        messageService.saveAssistantMessage(finalConversationId, aiResponse);

                        // 如果是第一次对话，生成标题
                        if (messageService.countBySessionId(finalConversationId) == 2) {
                            String autoTitle = generateTitle(prompt);
                            conversationService.updateConversationTitle(finalConversationId, autoTitle);
                        }

                        // 再次更新时间
                        conversationService.touchConversation(finalConversationId);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long conversationId) {
        Long targetConversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        ChatConversation conversation = conversationService.getById(targetConversationId);
        if (conversation == null) {
            return;
        }
        ChatScene scene = ChatScene.fromConversationType(conversation.getSessionType());
        ChatMemory chatMemory = chatClientRouter.getChatMemory(scene);

        messageService.removeBySessionId(targetConversationId);
        conversationService.removeById(targetConversationId);
        chatMemory.clear(targetConversationId.toString());
    }

    private Long ensureConversation(ChatScene scene, Long conversationId) {
        if (conversationId == null) {
            ChatConversation newConversation =
                    conversationService.createConversation("新对话", scene.getConversationType());
            return newConversation.getId();
        }

        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            ChatConversation newConversation =
                    conversationService.createConversation("新对话", scene.getConversationType());
            return newConversation.getId();
        }

        return conversationId;
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
