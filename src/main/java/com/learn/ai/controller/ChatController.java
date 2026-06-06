package com.learn.ai.controller;

import com.learn.ai.entity.ChatMessage;
import com.learn.ai.entity.ChatConversation;
import com.learn.ai.enums.ChatScene;
import com.learn.ai.service.ChatOrchestratorService;
import com.learn.ai.service.ChatConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ChatConversationService chatConversationService;

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
        return chatOrchestratorService.streamMessage(ChatScene.DEFAULT, conversationId, prompt);
    }

    /**
     * 获取所有会话列表
     * 
     * @return 包含所有历史会话的列表
     */
    @GetMapping("/sessions")
    public List<ChatConversation> getSessions() {
        return chatConversationService.getAllConversations();
    }

    /**
     * 创建新会话
     * 
     * @param request 包含 title 和 type 的请求体
     * @return 创建成功的会话实体
     */
    @PostMapping("/sessions")
    public ChatConversation createSession(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "新对话");
        String type = request.getOrDefault("type", "default");
        return chatConversationService.createConversation(title, type);
    }

    /**
     * 获取会话详情
     * 包含会话基本信息以及该会话下的所有历史消息
     * 
     * @param conversationId 会话唯一标识
     * @return 包含会话信息(session)和消息列表(messages)的Map
     */
    @GetMapping("/sessions/{conversationId}")
    public Map<String, Object> getSessionDetail(@PathVariable Long conversationId) {
        ChatConversation conversation = chatConversationService.getConversation(conversationId);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }
        
        List<ChatMessage> messages = chatConversationService.getConversationMessages(conversationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("session", conversation);
        result.put("messages", messages);
        
        return result;
    }

    /**
     * 删除指定会话
     * 
     * @param conversationId 待删除的会话ID
     * @return 操作结果反馈
     */
    @DeleteMapping("/sessions/{conversationId}")
    public Map<String, Object> deleteSession(@PathVariable Long conversationId) {
        try {
            chatOrchestratorService.deleteSession(conversationId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 更新会话标题
     * 
     * @param conversationId 目标会话ID
     * @param request 包含新 title 的请求体
     * @return 操作结果反馈
     */
    @PutMapping("/sessions/{conversationId}")
    public Map<String, Object> updateSession(@PathVariable Long conversationId, 
                                             @RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title != null && !title.trim().isEmpty()) {
            chatConversationService.updateConversationTitle(conversationId, title);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "更新成功");
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "标题不能为空");
        return result;
    }
}
