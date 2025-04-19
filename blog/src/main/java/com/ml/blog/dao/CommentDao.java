package com.ml.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ml.blog.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentDao extends BaseMapper<CommentEntity> {
}
