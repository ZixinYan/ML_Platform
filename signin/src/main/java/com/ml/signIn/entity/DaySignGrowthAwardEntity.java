package com.ml.signIn.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("day_sign_growth_award")
public class DaySignGrowthAwardEntity {

    @TableId

    // 连续签到天数
    private Integer continueDay;

    // 赠送成长值
    private Integer growthAwardAmount;

}
