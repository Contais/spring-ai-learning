package com.learn.ai.controller;

import com.learn.ai.entity.ChatMessage;
import com.learn.ai.entity.ChatSession;
//import com.learn.ai.service.ChatOrchestratorService;
import com.learn.ai.service.ChatOrchestratorService;
import com.learn.ai.service.ChatSessionService;
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
    private final ChatSessionService chatSessionService;

    /**
     * 流式聊天接口（demo）
     *
     * @param prompt 用户的提问内容
     * @param chatId 会话ID，可选。如果为空则使用 "default"
     * @return 响应内容的流 (SSE)
     */
    @PostMapping(value = "/chat/demo", produces = "text/html;charset=UTF-8")
    public Flux<String> demoChat(@RequestParam("prompt") String prompt,
                                   @RequestParam(value = "chatId", required = false) String chatId) {
        String sessionId = chatId != null ? chatId : "default";

        return chatClient.prompt()
                .user(prompt)
                .advisors(advice -> advice.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content();
    }


    /**
     * 流式聊天接口
     * 
     * @param prompt 用户的提问内容
     * @param chatId 会话ID，可选。如果为空则使用 "default"
     * @return 响应内容的流 (SSE)
     */
    @PostMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam("prompt") String prompt,
                                   @RequestParam(value = "chatId", required = false) String chatId) {
        String sessionId = chatId != null ? chatId : "default";
        return chatOrchestratorService.streamMessage(sessionId, prompt);
    }

    /**
     * 获取所有会话列表
     * 
     * @return 包含所有历史会话的列表
     */
    @GetMapping("/sessions")
    public List<ChatSession> getSessions() {
        return chatSessionService.getAllSessions();
    }

    /**
     * 创建新会话
     * 
     * @param request 包含 title 和 type 的请求体
     * @return 创建成功的会话实体
     */
    @PostMapping("/sessions")
    public ChatSession createSession(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "新对话");
        String type = request.getOrDefault("type", "default");
        return chatSessionService.createSession(title, type);
    }

    /**
     * 获取会话详情
     * 包含会话基本信息以及该会话下的所有历史消息
     * 
     * @param sessionId 会话唯一标识
     * @return 包含会话信息(session)和消息列表(messages)的Map
     */
    @GetMapping("/sessions/{sessionId}")
    public Map<String, Object> getSessionDetail(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        
        List<ChatMessage> messages = chatSessionService.getSessionMessages(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("session", session);
        result.put("messages", messages);
        
        return result;
    }

    /**
     * 删除指定会话
     * 
     * @param sessionId 待删除的会话ID
     * @return 操作结果反馈
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable String sessionId) {
        boolean deleted = chatSessionService.deleteSession(sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        result.put("message", deleted ? "删除成功" : "会话不存在");
        return result;
    }

    /**
     * 更新会话标题
     * 
     * @param sessionId 目标会话ID
     * @param request 包含新 title 的请求体
     * @return 操作结果反馈
     */
    @PutMapping("/sessions/{sessionId}")
    public Map<String, Object> updateSession(@PathVariable String sessionId, 
                                             @RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title != null && !title.trim().isEmpty()) {
            chatSessionService.updateSessionTitle(sessionId, title);
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
