package com.learn.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.ai.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {
}
