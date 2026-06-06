package com.learn.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.learn.ai.mapper")
public class SpringAiLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiLearningApplication.class, args);
    }

}
