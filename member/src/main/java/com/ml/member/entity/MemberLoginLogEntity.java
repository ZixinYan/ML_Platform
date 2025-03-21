package com.ml.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("member_login_log")
public class MemberLoginLogEntity implements Serializable {
    // 使用序列化存储
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private Long memberId;
    private Date createTime;
    private String ip;
    // APP or Web
    private Integer type;
}
