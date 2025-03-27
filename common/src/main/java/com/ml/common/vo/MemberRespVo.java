package com.ml.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
@Data
public class MemberRespVo implements Serializable {
    private Long id;
    private Long levelId;
    private String username;
    private String password;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private String socialUid;
    private Integer gender;
    private Integer integration;
    private Integer growth;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String accessToken;
    private Long expiresIn;
}
