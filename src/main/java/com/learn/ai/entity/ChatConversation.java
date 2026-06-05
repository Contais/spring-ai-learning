package com.learn.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
@TableName("chat_conversations")
public class ChatConversation {

    /**
     * 会话ID (使用MyBatis Plus雪花算法)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会话标题
     */
    @TableField("title")
    private String title;

    /**
     * 会话类型 (例如: default, translation, coding 等)
     */
    @TableField("session_type")
    private String sessionType;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标识 (0: 未删除, 1: 已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;
}
