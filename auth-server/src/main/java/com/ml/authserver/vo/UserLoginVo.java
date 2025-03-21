package com.ml.authserver.vo;

import lombok.Data;


@Data
public class UserLoginVo {
    private String loginAccount;
    private String passWord;
}
