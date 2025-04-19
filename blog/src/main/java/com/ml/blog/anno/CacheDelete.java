package com.ml.blog.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheDelete {
    /**
     * 一个或多个缓存 key，可以使用 SpEL 表达式
     * 支持模糊匹配，如 'blog:hot:*'
     */
    String[] keys();

    /**
     * 是否在方法执行前清除缓存（默认为 false）
     */
    boolean beforeInvocation() default false;
}
