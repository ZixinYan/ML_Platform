package com.ml.blog.anno;

import com.ml.blog.validation.StateValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented // 说明该注解将被包含在javadoc中
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {StateValidation.class}
)
public @interface State {
    String message() default "状态值不正确,只能是已发布或草稿";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
