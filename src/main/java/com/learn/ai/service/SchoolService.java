package com.learn.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.School;
import com.learn.ai.mapper.SchoolMapper;
import org.springframework.stereotype.Service;

/**
 * 校区表 服务类
 */
@Service
public class SchoolService extends ServiceImpl<SchoolMapper, School> {

}