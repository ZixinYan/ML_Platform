package com.ml.blog.config;

import com.ml.blog.entity.BlogEntity;
import com.ml.blog.vo.BlogSearchVo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
@Configuration
public interface BlogSearchRepository extends ElasticsearchRepository<BlogSearchVo, Long> {
    List<BlogSearchVo> findByTitleOrContent(String title, String content);

    void deleteById(Long id);
}
