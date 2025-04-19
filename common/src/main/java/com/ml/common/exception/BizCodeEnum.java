package com.ml.common.exception;

import lombok.Getter;

@Getter
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知错误"),
    VALID_EXCEPTION(10001, "参数校验异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    SMS_CODE_ERROR(10002, "验证码错误"),
    TO_MANY_REQUEST(10003, "请求流量过大，请稍后再试"),
    USER_NOT_LOGIN(10004, "用户未登录"),
    USER_EXISTS_EXCEPTION(15001, "用户名已存在"),
    EMAIL_EXISTS_EXCEPTION(15002, "邮箱已存在"),
    LOGIN_ACCOUNT_PASSWORD_INVALID(15003, "用户名或密码错误"),
    PHONE_EXISTS_EXCEPTION(15004,"手机号已存在"),
    REGISTER_ERROR(15005,"复杂错误"),
    THIRD_PARTY_ERROR(20000,"第三方服务调用失败"),
    BLOG_NOT_FOUND(60001,"博客不存在");


    private final Integer code;
    private final String msg;

    private BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}

