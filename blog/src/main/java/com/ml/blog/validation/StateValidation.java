package com.ml.blog.validation;

import com.ml.blog.anno.State;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StateValidation implements ConstraintValidator<State,String> {
    /**
     *
     * @param s 传入的值
     * @param constraintValidatorContext
     * @return
     */

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if("已发布".equals(s) || "草稿".equals(s)){
            return true;
        }
        return false;
    }
}
