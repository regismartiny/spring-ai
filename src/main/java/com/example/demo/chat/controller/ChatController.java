package com.example.demo.chat.controller;

import com.example.demo.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/chats")
public class ChatController {

    private final ChatService chatService;

    // 1️⃣ Envio de mensagem do usuário
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendMessage(@RequestParam String username,
                            @RequestParam String message,
                            @RequestPart(required = false) MultipartFile image,
                            @RequestParam(required = false) Boolean reset) throws IOException {
        chatService.sendMessage(username, message, image, reset);
    }

    // 2️⃣ SSE streaming do bot
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String username) {
        return chatService.stream(username);
    }

    /*
        Endpoint GET que retorna uma mensagem com base no nome do usuário informado
     */
    @GetMapping("/text")
    public String text(@RequestParam String username) {
        return chatService.text(username);
    }

    /*
        Endpoint de stream que retorna uma mensagem com base no nome do usuário informado
     */
    @GetMapping("/stream-text")
    public Flux<String> streamText(@RequestParam String username) {
        return chatService.streamText(username);
    }


}
