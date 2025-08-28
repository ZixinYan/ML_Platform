package com.ml.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @TableName t_order
 */
@TableName(value = "ml_order")
@Data
public class Order {
    /**
     * 订单Id
     */
    @TableId(type = IdType.AUTO)
    private Long OrderId;

    /**
     *
     */
    private Long memberId;

    /**
     *
     */
    private Long ProdId;

    /**
     *
     */
    private Long deliveryAddrId;

    /**
     *
     */
    private String ProdName;

    /**
     *
     */
    private Integer ProdCount;

    /**
     *
     */
    private BigDecimal ProdPrice;

    /**
     * 订单渠道 1pc，2Android，
     * 3ios
     */
    private Integer orderChannel;

    /**
     * 订单状态：0 新建未支付 1 已支付
     * 2 已发货 3 已收货 4 已完成 5 已取消 6 无效订单 7 售后中 8 售后完成
     */
    private Integer status;

    /**
     *
     */
    private Long createTime;

    /**
     *
     */
    private Long payTime;
}