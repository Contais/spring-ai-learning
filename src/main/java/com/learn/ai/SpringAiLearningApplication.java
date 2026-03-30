package com.learn.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiLearningApplication.class, args);
    }

    @Bean
    public ChatClient chatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .build();
    }

}
