package com.learn.ai.controller;

import com.learn.ai.enums.ChatScene;
import com.learn.ai.service.ChatOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 聊天核心控制器
 * 处理流式对话、会话管理等核心业务
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatOrchestratorService chatOrchestratorService;
    private final ChatClient chatClient;

    /**
     * 流式聊天接口 (直接使用 ChatClient)
     * 
     * @param prompt 用户的提问内容
     * @param conversationId 会话ID，可选。
     * @return 响应内容的流 (SSE)
     */
    @PostMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam("prompt") String prompt,
                                   @RequestParam(value = "conversationId", required = false) Long conversationId) {
        final String cid = conversationId.toString();
        return chatClient.prompt()
                .user(prompt)
                .advisors(advice -> advice.param(ChatMemory.CONVERSATION_ID, cid))
                .stream()
                .content();
    }

    /**
     * 流式聊天接口 (通过 OrchestratorService)
     * 
     * @param prompt 用户的提问内容
     * @param conversationId 会话ID，必传
     * @return 响应内容的流 (SSE)
     */
    @PostMapping(value = "/chat/orchestrated", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChatOrchestrated(@RequestParam("prompt") String prompt,
                                               @RequestParam(value = "conversationId", required = false) Long conversationId) {
        return chatOrchestratorService.streamMessage(ChatScene.CHAT, conversationId, prompt);
    }

}
