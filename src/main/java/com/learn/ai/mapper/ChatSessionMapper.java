package com.learn.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.ai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}