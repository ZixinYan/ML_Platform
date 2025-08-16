package com.product.entity;

import lombok.Data;

@Data
public class FlashSaleProdInfo extends ProdInfo{
    /**
     * 秒杀开始时间
     */
    private Long startTime;
    /**
     * 秒杀结束时间
     */
    private Long endTime;
    /**
     * 秒杀库存
     */
    private Integer stock;

}
