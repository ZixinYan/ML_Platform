package com.ml.aiservice.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EmbeddingStoreConfig {
    @Value("${vector.pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${vector.pinecone.index-name}")
    private String pineconeIndexName;

    @Value("${vector.pinecone.namespace}")
    private String pineconeNameSpace;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(){
        return PineconeEmbeddingStore.builder()
                .apiKey(pineconeApiKey)
                .index(pineconeIndexName)
                .nameSpace(pineconeNameSpace)
                .createIndex(PineconeServerlessIndexConfig.builder()
                        .cloud("AWS")
                        .region("us-east-1")
                        .dimension(embeddingModel.dimension())
                        .build())
                .build();
    }
}
