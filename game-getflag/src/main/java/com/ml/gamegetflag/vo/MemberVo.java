package com.ml.gamegetflag.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class MemberVo {
    private Long id;
    private Integer integration;
    private Integer growth;
    public Integer getIntegration() {
        return (integration != null) ? integration : 0;
    }

    public Integer getGrowth() {
        return (growth != null) ? growth : 0;
    }

}
