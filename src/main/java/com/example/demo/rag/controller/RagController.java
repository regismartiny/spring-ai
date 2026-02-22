package com.example.demo.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/rags")
public class RagController {

    private final ChatClient chatClient;

    public RagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String question) {
        return chatClient.prompt().user(question).call().content();
    }

    @GetMapping(path = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String question) {
        return chatClient.prompt().user(question).stream()
                .content()
                .scan(new StringBuilder(), (acc, token) -> {
                    acc.append(token);   // apenas acumula
                    return acc;
                })
                .map(StringBuilder::toString); // envia texto completo até agor
    }
}
