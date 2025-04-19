package com.ml.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.blog.entity.BlogEntity;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.R;

import java.util.Map;

public interface BlogService extends IService<BlogEntity> {
    R queryBlogById(Long id);

    R queryBlogLikesById(Long id);

    PageUtils queryPage(Map<String, Object> params);

    boolean updateBlog(BlogEntity blog);

    R likeBlog(Long id);

    R queryHotBlog(Integer current);

    boolean deleteBlog(Long id);

    PageUtils searchBlog(Integer current, String[] keyword);
}
