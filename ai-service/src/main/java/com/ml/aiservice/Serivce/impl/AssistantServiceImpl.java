package com.ml.aiservice.Serivce.impl;

import com.ml.aiservice.Serivce.AssistantService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssistantServiceImpl implements AssistantService {


    /*
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

     */

    /**
     * 上传知识库
     * @param files
     */
    /*
    @Override
    public void uploadKnowledgeLibrary(File[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("文件不能为空");
        }

        List<Document> documents = Arrays.stream(files)
                .map(file -> {
                    Document doc = FileSystemDocumentLoader.loadDocument(file.getAbsolutePath());
                    // 来源文件名
                    doc.metadata().put("source", file.getName());
                    return doc;
                })
                .collect(Collectors.toList());

        EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build()
                .ingest(documents);
    }

     */

    /**
     * 重新上传（覆盖）某个文档
     */
    /*
    @Override
    public void reUploadKnowledge(String documentId, File file) {
        // 1. 删除原来对应的向量（如果 embeddingStore 支持删除）
        if (embeddingStore instanceof PineconeEmbeddingStore pineconeStore) {
            pineconeStore.remove(documentId);
        }

        // 2. 加载新文件
        Document document = FileSystemDocumentLoader.loadDocument(file.getAbsolutePath());
        document.metadata().put("source", file.getName());

        // 3. 重新上传
        EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build()
                .ingest(Collections.singletonList(document));
    }
    */
}

