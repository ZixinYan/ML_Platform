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
     * 签到日期年月(yyyyMM)
     */
    private String yearMonth;
    /**
     * 签到状态
     */
    private String signStatus;
}
