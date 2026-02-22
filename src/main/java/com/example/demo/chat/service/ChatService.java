package com.example.demo.chat.service;

import com.example.demo.chat.model.PromptMedia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Validated
@Service
public class ChatService {

    private final ChatClient chatClient;

    private final Map<String, String> messageCache = new ConcurrentHashMap<>();
    private final Map<String, PromptMedia> mediaCache = new ConcurrentHashMap<>();

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        chatClient = chatClientBuilder
                .defaultSystem("Sempre responda no idioma Português")
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public void sendMessage(@NotNull String username, @NotNull String message, MultipartFile image, Boolean reset) throws IOException {
        if (Boolean.TRUE.equals(reset)) {
            clearCacheForUser(username);
            return;
        }

        messageCache.put(username, message);

        if (image != null && !image.isEmpty()) {
            cacheImage(username, image);
        }
    }

    public Flux<String> stream(@NotNull String username) {
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

    public String text(@NotNull String username) {
        return chatClient.prompt().user("Hi my name is " + username).call().content();
    }

    public Flux<String> streamText(@NotNull String username) {
        return chatClient.prompt().user("Hi my name is " + username).stream().content().map(token -> {
            // adiciona espaço antes do token se não for pontuação
            if (token.matches("^[,.;!?—]$")) {
                return token; // pontuação vai grudada
            } else {
                return " " + token; // palavras separadas
            }
        });
    }

    private void cacheImage(String username, MultipartFile image) throws IOException {
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
            throw e;
        }
    }

    private void clearCacheForUser(String username) {
        messageCache.remove(username);
        mediaCache.remove(username);
    }
}
