package com.ml.blog.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CommentVo {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long blogId;
    private Long parentId;
    private Long answerId;
    private String answerName;
    private String answerAvatar;
    private Date createTime;
    private List<CommentVo> replies;  // 用于存储子评论
}
