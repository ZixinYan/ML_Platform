package com.ml.member.entity;

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

    //TO yzx :就是一个键值对的表，用来存储连续签到天数和赠送成长值的关系，大概就是第一天给10成长值，第二天给20成长值之类的，这个具体得多少分没设计，我怕你积分通货膨胀了
    //或者是你写个接口，从管理员权限前端能传进来也行
}
