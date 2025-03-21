package com.ml.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("member_level")
public class MemberLevelEntity {
    @TableId
    private Long id;
    private String name;
    private Integer growthPoint;
    private Integer defaultStatus;
    private String note;
}
