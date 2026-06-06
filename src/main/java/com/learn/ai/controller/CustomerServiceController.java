package com.learn.ai.controller;

import com.learn.ai.enums.ChatScene;
import com.learn.ai.service.ChatOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customerService")
public class CustomerServiceController {

    private final ChatOrchestratorService chatOrchestratorService;

    @PostMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChatOrchestrated(@RequestParam("prompt") String prompt,
                                               @RequestParam("conversationId") Long conversationId) {
        return chatOrchestratorService.streamMessage(ChatScene.CUSTOMER_SERVICE, conversationId, prompt);
    }
}
