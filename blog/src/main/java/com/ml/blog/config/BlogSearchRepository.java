package com.ml.blog.config;

import com.ml.blog.entity.BlogEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
@Configuration
public interface BlogSearchRepository extends ElasticsearchRepository<BlogEntity, Long> {
    List<BlogEntity> findByTitleOrContent(String title, String content);
}
