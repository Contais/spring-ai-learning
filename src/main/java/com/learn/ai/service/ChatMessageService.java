package com.learn.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.ChatMessage;
import com.learn.ai.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    /**
     * 保存用户消息
     */
    public ChatMessage saveUserMessage(Long conversationId, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole("user");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }

    /**
     * 保存 AI 助手消息
     */
    public ChatMessage saveAssistantMessage(Long conversationId, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole("assistant");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }

    /**
     * 根据会话ID查询所有消息（按时间正序）
     */
    public List<ChatMessage> listBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 分页查询会话消息（基于游标）
     */
    public List<ChatMessage> listMessagesBefore(Long conversationId, Long beforeId, int limit) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);

        if (beforeId != null) {
            wrapper.lt(ChatMessage::getId, beforeId);
        }

        wrapper.orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit);

        return list(wrapper);
    }

    /**
     * 删除会话的所有消息
     */
    public boolean removeBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        return remove(wrapper);
    }

    /**
     * 统计会话的消息数量
     */
    public long countBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        return count(wrapper);
    }
}
