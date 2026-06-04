
package com.learn.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.ai.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存用户消息
     */
    ChatMessage saveUserMessage(Long conversationId, String content);

    /**
     * 保存 AI 助手消息
     */
    ChatMessage saveAssistantMessage(Long conversationId, String content);

    /**
     * 根据会话ID查询所有消息（按时间正序）
     */
    List<ChatMessage> listBySessionId(Long conversationId);

    /**
     * 分页查询会话消息（基于游标）
     */
    List<ChatMessage> listMessagesBefore(Long conversationId, Long beforeId, int limit);

    /**
     * 删除会话的所有消息
     */
    boolean removeBySessionId(Long conversationId);

    /**
     * 统计会话的消息数量
     */
    long countBySessionId(Long conversationId);
}