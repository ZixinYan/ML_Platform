package com.ml.blog.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.ml.blog.anno.State;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("blog_info")
@Document(indexName = "blog")
public class BlogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotEmpty(message = "文章标题不能为空")
    @Pattern(regexp = "^[\\s\\S]{1,10}$",message = "文章标题长度必须在1-10之间")
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;//文章标题

    @NotEmpty(message = "文章内容不能为空")
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;//文章内容

    @TableField(exist = false)
    private Boolean isLike;//是否点赞

    @TableField(exist = false)
    private String userName;//点赞账户名

    @TableField(exist = false)
    private String userAvatar;//用户头像
    // 文章点赞数量
    private Long liked;
    // 评论数量
    private Long comments;
    // 文章内图片
    private String images;
    private String coverImg;//封面图像
    @State
    private String state;//发布状态 已发布|草稿
    @NotNull(message = "文章分类ID不能为空")
    private Long categoryId;//文章分类id
    private Long createUser;//创建人ID
    private Date createTime;//创建时间
    private Date updateTime;//更新时间

    /**
     * 逻辑删除标识（0：正常，1：已删除）
     */
    @TableLogic
    private Integer status;

    @Version
    private Integer version;
}
