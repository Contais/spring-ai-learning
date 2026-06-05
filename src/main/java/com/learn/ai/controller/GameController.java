package com.learn.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final ChatClient gameChatClient;

    @PostMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChatOrchestrated(@RequestParam("prompt") String prompt,
                                               @RequestParam("conversationId") Long conversationId) {
        final String cid = conversationId.toString();
        return gameChatClient.prompt()
                .user(prompt)
                .advisors(advice -> advice.param(ChatMemory.CONVERSATION_ID, cid))
                .stream()
                .content();
    }
}
