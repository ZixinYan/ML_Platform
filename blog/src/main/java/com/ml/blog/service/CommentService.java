package com.ml.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.blog.entity.CommentEntity;
import com.ml.common.utils.R;

public interface CommentService extends IService<CommentEntity> {
    R getComments(Long id);

    R deleteComments(Long id);
}
