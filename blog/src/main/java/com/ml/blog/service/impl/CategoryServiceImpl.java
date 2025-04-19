package com.ml.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ml.blog.dao.BlogDao;
import com.ml.blog.dao.CategoryDao;
import com.ml.blog.entity.BlogEntity;
import com.ml.blog.entity.CategoryEntity;
import com.ml.blog.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
}
