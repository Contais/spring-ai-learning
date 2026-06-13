package com.learn.ai.controller;

import com.learn.ai.entity.ChatConversation;
import com.learn.ai.model.vo.MessageVO;
import com.learn.ai.model.vo.Result;
import com.learn.ai.service.ChatConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器
 * 提供会话的创建、删除、查询以及消息查询功能
 */
@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatConversationService conversationService;

    /**
     * 创建新会话
     *
     * @param title       会话标题（可选）
     * @param sessionType 会话类型
     * @return 创建成功的会话对象
     */
    @PostMapping("/add")
    public Result<ChatConversation> addConversation(@RequestParam(required = false) String title,
                                                    @RequestParam String sessionType) {
        try {
            ChatConversation conversation = conversationService.createConversation(title, sessionType);
            return Result.ok(conversation);
        } catch (Exception e) {
            return Result.fail("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话及其所有消息
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<Long> deleteConversation(@RequestParam Long conversationId) {
        try {
            boolean deleted = conversationService.deleteConversation(conversationId);
            if (deleted) {
                return Result.ok(conversationId);
            } else {
                return Result.fail("会话不存在");
            }
        } catch (Exception e) {
            return Result.fail("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话列表（按更新时间倒序）
     *
     * @param sessionType 会话类型（可选，不传则返回所有类型）
     * @return 会话列表
     */
    @GetMapping("/list")
    public Result<List<ChatConversation>> listConversations(@RequestParam(required = false) String sessionType) {
        if (sessionType != null && !sessionType.isEmpty()) {
            return Result.ok(conversationService.getConversationsByType(sessionType));
        } else {
            return Result.ok(conversationService.getAllConversations());
        }
    }

    /**
     * 获取会话的消息列表
     *
     * @param conversationId 会话ID
     * @param sessionType    会话类型
     * @return 消息列表
     */
    @GetMapping("/messages")
    public Result<List<MessageVO>> getMessages(@RequestParam Long conversationId,
                                               @RequestParam String sessionType) {
        return Result.ok(conversationService.getConversationMessageVOs(conversationId, sessionType));
    }
}
