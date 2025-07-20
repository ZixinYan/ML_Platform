package com.ml.common.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author yaoxinjia
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    Set<Integer> set = new HashSet<Integer>();
    /**
     * 初始化
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * 真正的校验规则
     * 判断是否校验成功
     * @param integer
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}
