package com.example.demo.controller;

import com.example.demo.model.PromptMedia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping(path = "/chats")
public class ChatController {

    private final ChatClient chatClient;

    private final Map<String, String> messageCache = new ConcurrentHashMap<>();
    private final Map<String, PromptMedia> mediaCache = new ConcurrentHashMap<>();

    public ChatController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        chatClient = chatClientBuilder
                .defaultSystem("Sempre responda no idioma Português")
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    // 1️⃣ Envio de mensagem do usuário
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendMessage(@RequestParam String username,
                            @RequestParam(required = false) String message,
                            @RequestPart(required = false) MultipartFile image,
                            @RequestParam(required = false) Boolean reset) {
        if (Boolean.TRUE.equals(reset)) {
            clearCacheForUser(username);
            return;
        }

        if (message != null && !message.isEmpty()) {
            messageCache.put(username, message);
        }

        if (image != null && !image.isEmpty()) {
            try {
                String base64 = Base64.getEncoder().encodeToString(image.getBytes());
                String mime = image.getContentType();

                if (isNull(mime) || !mime.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid image type: " + mime);
                }

                byte[] bytes = Base64.getDecoder().decode(base64);
                ByteArrayResource resource = new ByteArrayResource(bytes);
                MimeType mimeType = MimeTypeUtils.parseMimeType(mime);
                PromptMedia promptMedia = new PromptMedia(mimeType, resource);

                mediaCache.put(username, promptMedia);
            } catch (Exception e) {
                log.error("Erro ao enviar imagem: {}", e.getMessage());
            }
        }
    }

    // 2️⃣ SSE streaming do bot
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String username) {
        String userMessage = Optional.ofNullable(messageCache.get(username)).orElseThrow(
                () -> new IllegalArgumentException("No message found for user: " + username)
        );
        PromptMedia promptMedia = mediaCache.get(username);

        clearCacheForUser(username);

        // Stream do bot
       return chatClient.prompt()
               .user(u -> {
                    if (nonNull(promptMedia)) {
                        u.text(userMessage);
                        u.media(promptMedia.mimeType(), promptMedia.resource());
                    } else {
                        u.text(userMessage);
                    }
               })
               .stream()
               .content()
               .scan(new StringBuilder(), (acc, token) -> {
                   acc.append(token);   // apenas acumula
                   return acc;
               })
               .map(StringBuilder::toString); // envia texto completo até agora;
    }
    private void clearCacheForUser(String username) {
        messageCache.remove(username);
        mediaCache.remove(username);
    }

    /*
        Endpoint GET que retorna uma mensagem com base no nome do usuário informado
     */
    @GetMapping("/text")
    public String text(@RequestParam String username) {

        return chatClient.prompt().user("Hi my name is " + username).call().content();
    }

    /*
        Endpoint de stream que retorna uma mensagem com base no nome do usuário informado
     */
    @GetMapping("/stream-text")
    public Flux<String> streamText(@RequestParam String username) {

        return chatClient.prompt().stream().content().map(token -> {
            // adiciona espaço antes do token se não for pontuação
            if (token.matches("^[,.;!?—]$")) {
                return token; // pontuação vai grudada
            } else {
                return " " + token; // palavras separadas
            }
        });
    }


}
