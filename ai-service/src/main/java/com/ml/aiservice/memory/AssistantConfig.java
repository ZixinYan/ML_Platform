package com.ml.aiservice.memory;


import cn.hutool.core.io.resource.ClassPathResource;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class AssistantConfig {
    @Autowired
    private AssistantMemory assistantMemory;

    @Autowired
    private EmbeddingStore embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Bean(name = "assistantMemoryProvider")
    public ChatMemoryProvider assistantMemoryProvider(){
        return chatId -> MessageWindowChatMemory.builder()
                .id(chatId)
                .maxMessages(100)
                .chatMemoryStore(assistantMemory)
                .build();
    }

    /**
     * 创建本地一个内存聊天存储
     * @return
     */
    /*
    @Bean(name = "contentRetriever")
    public ContentRetriever contentRetriever() {
        // 从 classpath 获取资源文件并转换为真实的 File path
        String absolutePath = new ClassPathResource("knowledge_base/platform.md")
                .getFile()
                .getAbsolutePath();

        Document document = FileSystemDocumentLoader.loadDocument(absolutePath);
        List<Document> documents = Arrays.asList(document);
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

     */



    /**
     * 創建pinecone向量數據庫
     */

    @Bean(name = "contentRetriever")
    public ContentRetriever contentRetriever(){
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(10) // 返回的最大结果数
                .minScore(0.7) // 最小分数
                .build();
    }
}
