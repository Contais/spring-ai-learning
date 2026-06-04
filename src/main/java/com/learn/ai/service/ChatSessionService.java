package com.learn.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.ChatMessage;
import com.learn.ai.entity.ChatSession;
import com.learn.ai.mapper.ChatMessageMapper;
import com.learn.ai.mapper.ChatSessionMapper;
import com.learn.ai.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天会话服务类
 * 处理会话的创建、查询、更新、删除以及消息的持久化
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    /**
     * 创建新会话
     * 
     * @param title 会话标题
     * @param sessionType 会话类型
     * @return 创建成功的会话对象
     */
    public ChatSession createSession(String title, String sessionType) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setTitle(title != null ? title : "新对话");
        session.setSessionType(sessionType);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 获取所有会话列表（按更新时间倒序）
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getAllSessions() {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatSession::getUpdatedAt);
        return sessionMapper.selectList(wrapper);
    }

    /**
     * 根据类型获取会话列表
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getSessionsByType(String sessionType) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getSessionType, sessionType)
               .orderByDesc(ChatSession::getUpdatedAt);
        return sessionMapper.selectList(wrapper);
    }

    /**
     * 获取单个会话
     */
    @Transactional(readOnly = true)
    public ChatSession getSession(String sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    /**
     * 获取会话的所有消息
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getSessionMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreatedAt);
        return messageMapper.selectList(wrapper);
    }

    /**
     * 根据会话ID和类型获取消息
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getSessionMessages(String sessionId, String sessionType) {
        // 先检查会话是否存在且类型匹配
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !sessionType.equals(session.getSessionType())) {
            return List.of();
        }
        return getSessionMessages(sessionId);
    }

    /**
     * 根据会话ID和类型获取消息，并转换为 MessageVO 列表
     */
    @Transactional(readOnly = true)
    public List<MessageVO> getSessionMessageVOs(String sessionId, String sessionType) {
        List<ChatMessage> messages = getSessionMessages(sessionId, sessionType);
        return messages.stream()
                .map(m -> new MessageVO(m.getRole(), m.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 删除会话及其所有消息
     */
    public boolean deleteSession(String sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return false;
        }
        
        // 删除会话的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(wrapper);
        
        // 删除会话
        sessionMapper.deleteById(sessionId);
        return true;
    }

    /**
     * 更新会话标题
     */
    public void updateSessionTitle(String sessionId, String title) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSession::getId, sessionId)
               .set(ChatSession::getTitle, title)
               .set(ChatSession::getUpdatedAt, LocalDateTime.now());
        sessionMapper.update(null, wrapper);
    }

    /**
     * 保存消息到会话
     */
    public ChatMessage saveMessage(String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        // 更新会话时间戳
        touchSession(sessionId);
        
        return message;
    }

    /**
     * 更新会话时间戳
     */
    public void touchSession(String sessionId) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSession::getId, sessionId)
               .set(ChatSession::getUpdatedAt, LocalDateTime.now());
        sessionMapper.update(null, wrapper);
    }
}
