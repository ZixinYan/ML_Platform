package com.ml.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@TableName("member_info")
public class MemberEntity {
    @TableId
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
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String accessToken;
    private Long expiresIn;
}
