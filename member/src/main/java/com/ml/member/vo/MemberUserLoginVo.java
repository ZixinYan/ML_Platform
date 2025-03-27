package com.ml.member.vo;

import lombok.Data;


@Data
public class MemberUserLoginVo {
    private String userName;
    private String password;
    private Integer type;
    private String IP;
}
