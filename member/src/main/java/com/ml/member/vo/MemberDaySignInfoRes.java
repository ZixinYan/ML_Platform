package com.ml.member.vo;

import lombok.Data;

import java.util.List;

@Data
public class MemberDaySignInfoRes {
    /**
     * 连续签到天数
     */
    private Integer continueDay;
    /**
     * 签到日历列表
     */
    private List<MemberDaySignRes> calendarList;
}
