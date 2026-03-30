package com.learn.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController("/ai")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @RequestMapping("/chat")
    public String chat(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @RequestMapping(value = "/chat/stream", produces = "text/event-stream")
    public Flux<String> streamChat(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }


}
