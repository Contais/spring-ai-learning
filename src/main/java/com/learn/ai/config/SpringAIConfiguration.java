package com.learn.ai.config;


import com.learn.ai.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

import javax.sql.DataSource;

@Configuration
public class SpringAIConfiguration {

    @Value("classpath:prompts/game-system-prompt.md")
    private Resource gameSystemPrompt;

    @Value("classpath:prompts/customer-service-system-prompt.md")
    private Resource customerServiceSystemPrompt;

    /**
     * 基于数据库的聊天记忆仓库
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository(DataSource dataSource) {
        return JdbcChatMemoryRepository.builder()
                .dataSource(dataSource)
                .build();
    }

    /**
     * 聊天记忆（使用内存记忆）
     */
    @Bean
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }


    /**
     * 聊天记忆（使用数据库存储）
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    /**
     * ChatClient
     */
    @Bean
    public ChatClient chatClient(DeepSeekChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder().model("deepseek-reasoner").build())
                .defaultSystem("你是贴心的小助手，你的名字叫可可宝贝。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * GameChatClient
     */
    @Bean
    public ChatClient gameChatClient(DeepSeekChatModel model, ChatMemory inMemoryChatMemory) throws Exception {
        return ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder().model("deepseek-reasoner").build())
                .defaultSystem(gameSystemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build()
                )
                .build();
    }

    /**
     * CustomerServiceChatClient
     */
    @Bean
    public ChatClient customerServiceChatClient(DeepSeekChatModel model, ChatMemory chatMemory, CourseTools courseTools) throws Exception {
        return ChatClient.builder(model)
                .defaultSystem(customerServiceSystemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(courseTools)
                .build();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel model) {
        return SimpleVectorStore.builder(model).build();
    }

}
