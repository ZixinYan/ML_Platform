package com.ml.gamegetflag.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GrowthChangeHistoryVo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private Long memberId;
        private Date createTime;
        /**
         * 	改变的值（正负计数）
         */
        private Integer changeCount;
        private String note;
        /**
         * 积分来源[0-游戏，1-管理员修改]
         */
        private Integer sourceType;
    }

