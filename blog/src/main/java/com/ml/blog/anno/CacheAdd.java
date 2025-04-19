package com.ml.blog.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAdd {
    String key();
    long ttl() default 30;
    TimeUnit unit() default TimeUnit.MINUTES;
}

