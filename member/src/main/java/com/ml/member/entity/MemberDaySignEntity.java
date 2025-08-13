package com.ml.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("member_day_sign")
public class MemberDaySignEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long memberId;
    /**
     * 签到日期(yyyyMMdd)
     */
    private String signDate;
    /**
     *连续签到天数
     */
    private Integer continueDay;
    /**
     * 本轮初始签到日期
     */
    private String startSignDate;
    /**
     * 签到状态
     */
    private int signStatus;

    public MemberDaySignEntity(Long memberId, String signDate, int signStatus) {
        this.memberId = memberId;
        this.signDate = signDate;
        this.signStatus = signStatus;
    }
}
