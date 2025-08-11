package com.ml.member.vo;

import lombok.Data;

@Data
public class MemberDaySignRes {
    /**
     * 签到日期
     */
    private String signDate;
    /**
     * 签到状态 0 未签到 1 已签到
     */
    private Integer signStatus;
    /**
     * 成长值奖励
     */
    private Integer growthRewardAmount;

}
