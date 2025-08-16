package com.ml.signIn.vo;

import lombok.Data;

@Data
public class MemberDaySignInfoRes {
    /**
     * 连续签到天数
     */
    private Integer continueDay;
    /**
     * 签到日历列表
     */
    private String signStatusStr;
    /**
     * 本轮连续签到初始日期
     */
    private String startSingDate;
}
