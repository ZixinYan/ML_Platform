package com.ml.member.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class IntegrationChangeHistoryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     * create_time
     */
    private Date createTime;
    /**
     * 变化的值
     */
    private Integer changeCount;
    /**
     * 备注
     */
    private String note;
    /**
     * 来源[0->游戏；1->管理员修改;2->活动]
     */
    private Integer sourceType;
}
