package com.example.demo.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "rag.ingest.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class IngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Value("classpath:/docs/article.pdf")
    private Resource pdfResource;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    @Override
    public void run(String... args) {
        log.info("Starting data ingestion process...");
        var pdfreader= new ParagraphPdfDocumentReader(pdfResource);
        TextSplitter splitter = new TokenTextSplitter();
        vectorStore.accept(splitter.apply(pdfreader.get()));
        log.info("Vector store updated with PDF content.");
    }
}
