package com.ml.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ml.blog.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
}
