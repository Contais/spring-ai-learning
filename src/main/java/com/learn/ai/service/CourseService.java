package com.learn.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.ai.entity.Course;
import com.learn.ai.mapper.CourseMapper;
import org.springframework.stereotype.Service;

/**
 * 学科表 服务类
 */
@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

}