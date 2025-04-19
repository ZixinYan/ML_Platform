package com.ml.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberRespVo {
    private Long id;
    private Long levelId;
    private String username;
    private String nickname;
    private String mobile;
    private String avatar;
    private String socialUid;
    private Integer gender;
    private Integer integration;
    private Integer growth;
    private String accessToken;
    private Long expiresIn;
}
