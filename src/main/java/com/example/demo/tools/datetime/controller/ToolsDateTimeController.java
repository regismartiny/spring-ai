package com.example.demo.tools.datetime.controller;

import com.example.demo.tools.datetime.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tools/datetime")
public class ToolsDateTimeController {

    private final ChatClient chatClient;

    public ToolsDateTimeController(ChatClient.Builder builder) {
        this.chatClient = builder
                .build();
    }

    @GetMapping
    public String tools() {
        return chatClient.prompt("Que dia será amanhã?")
                .tools(new DateTimeTools())
                .call()
                .content();
    }
}
