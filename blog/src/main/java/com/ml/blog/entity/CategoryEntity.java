package com.ml.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.Date;

@Data
@TableName("blog_category")
public class CategoryEntity {
    @NotNull(message = "ID不能为空",groups = {Update.class})
    private Long id;//主键ID
    @NotEmpty(message = "分类名称不能为空",groups = {Add.class,Update.class})
    private String categoryName;//分类名称
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
    @NotEmpty
    private String content;//分类描述
    public interface Add extends Default {};
    public interface Update extends Default{};
}
