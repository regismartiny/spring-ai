package com.example.demo.model;

import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;

public record PromptMedia(MimeType mimeType, Resource resource) {
}
