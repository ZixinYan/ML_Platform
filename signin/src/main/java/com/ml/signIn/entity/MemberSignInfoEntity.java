package com.ml.signIn.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.BitSet;

/**
 * 会员签到信息封装类
 * 用于将签到相关数据序列化存储到Redis
 */
@Data
@TableName("member_sign_info")
public class MemberSignInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 签到状态BitSet，每个bit代表一天是否签到
    private BitSet signStatus;
    
    // 最后签到日期
    private String lastSignDate;
    
    // 连续签到天数
    private int continueDays;
    
    // 连续签到开始日期
    private String startSignDate;
    
    // 构造方法
    public MemberSignInfoEntity() {
        this.signStatus = new BitSet();
        this.continueDays = 0;
    }
}