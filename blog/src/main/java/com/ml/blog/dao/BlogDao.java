package com.ml.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ml.blog.entity.BlogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogDao extends BaseMapper<BlogEntity> {
}
