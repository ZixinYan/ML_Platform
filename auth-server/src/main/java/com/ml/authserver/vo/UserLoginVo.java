package com.ml.authserver.vo;

import lombok.Data;


@Data
public class UserLoginVo {
    private String userName;
    private String password;
    private Integer type;
    private String IP;
}
