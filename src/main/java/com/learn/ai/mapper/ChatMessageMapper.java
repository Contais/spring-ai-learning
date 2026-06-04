package com.learn.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
