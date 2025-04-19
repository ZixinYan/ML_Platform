package com.ml.blog.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("blog_comment")
public class CommentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String content;

    private Long userId;

    private Long blogId;
    /**
     * 0是一级评论
     */
    private Long parentId;

    private Long answerId;

    private Date createTime;

    /**
     * 逻辑删除标识（0：正常，1：已删除）
     */
    @TableLogic
    private Integer status;
}
