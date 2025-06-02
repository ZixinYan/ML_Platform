package com.ml.codehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @TableName t_codes
 */
@TableName(value = "ml_codes")
@Data
public class Codes implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 代码 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private String codesName;

    /**
     * 代码标题
     */
    private String codesTitle;

    /**
     * 代码详情
     */
    private String codesDetail;

    /**
     * 代码价格
     */
    private BigDecimal codesPrice;

    /**
     * 代码库存
     */
    private Integer codesStock;


}