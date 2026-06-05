
package com.learn.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.ChatMessage;
import com.learn.ai.mapper.ChatMessageMapper;
import com.learn.ai.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Override
    public ChatMessage saveUserMessage(Long conversationId, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole("user");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }

    @Override
    public ChatMessage saveAssistantMessage(Long conversationId, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole("assistant");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }

    @Override
    public List<ChatMessage> listBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt);
        return list(wrapper);
    }

    @Override
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

    @Override
    public boolean removeBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        return remove(wrapper);
    }

    @Override
    public long countBySessionId(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        return count(wrapper);
    }
}