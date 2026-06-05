package com.learn.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.ChatMessage;
import com.learn.ai.entity.ChatConversation;
import com.learn.ai.mapper.ChatMessageMapper;
import com.learn.ai.mapper.ChatConversationMapper;
import com.learn.ai.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天会话服务类
 * 处理会话的创建、查询、更新、删除以及消息的持久化
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatConversationService extends ServiceImpl<ChatConversationMapper, ChatConversation> {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;

    /**
     * 创建新会话
     * 
     * @param title 会话标题
     * @param sessionType 会话类型
     * @return 创建成功的会话对象
     */
    public ChatConversation createConversation(String title, String sessionType) {
        ChatConversation conversation = new ChatConversation();
        conversation.setTitle(title != null ? title : "新对话");
        conversation.setSessionType(sessionType);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * 获取所有会话列表（按更新时间倒序）
     */
    @Transactional(readOnly = true)
    public List<ChatConversation> getAllConversations() {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatConversation::getUpdatedAt);
        return conversationMapper.selectList(wrapper);
    }

    /**
     * 根据类型获取会话列表
     */
    @Transactional(readOnly = true)
    public List<ChatConversation> getConversationsByType(String sessionType) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getSessionType, sessionType)
               .orderByDesc(ChatConversation::getUpdatedAt);
        return conversationMapper.selectList(wrapper);
    }

    /**
     * 获取单个会话
     */
    @Transactional(readOnly = true)
    public ChatConversation getConversation(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    /**
     * 获取会话的所有消息
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationMessages(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByAsc(ChatMessage::getCreatedAt);
        return messageMapper.selectList(wrapper);
    }

    /**
     * 根据会话ID和类型获取消息
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationMessages(Long conversationId, String sessionType) {
        // 先检查会话是否存在且类型匹配
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !sessionType.equals(conversation.getSessionType())) {
            return List.of();
        }
        return getConversationMessages(conversationId);
    }

    /**
     * 根据会话ID和类型获取消息，并转换为 MessageVO 列表
     */
    @Transactional(readOnly = true)
    public List<MessageVO> getConversationMessageVOs(Long conversationId, String sessionType) {
        List<ChatMessage> messages = getConversationMessages(conversationId, sessionType);
        return messages.stream()
                .map(m -> new MessageVO(m.getRole(), m.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 删除会话及其所有消息
     */
    public boolean deleteConversation(Long conversationId) {
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return false;
        }
        
        // 删除会话的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        messageMapper.delete(wrapper);
        
        // 删除会话
        conversationMapper.deleteById(conversationId);
        return true;
    }

    /**
     * 更新会话标题
     */
    public void updateConversationTitle(Long conversationId, String title) {
        LambdaUpdateWrapper<ChatConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatConversation::getId, conversationId)
               .set(ChatConversation::getTitle, title)
               .set(ChatConversation::getUpdatedAt, LocalDateTime.now());
        conversationMapper.update(null, wrapper);
    }

    /**
     * 保存消息到会话
     */
    public ChatMessage saveMessage(Long conversationId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        // 更新会话时间戳
        touchConversation(conversationId);
        
        return message;
    }

    /**
     * 更新会话时间戳
     */
    public void touchConversation(Long conversationId) {
        LambdaUpdateWrapper<ChatConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatConversation::getId, conversationId)
               .set(ChatConversation::getUpdatedAt, LocalDateTime.now());
        conversationMapper.update(null, wrapper);
    }
}
