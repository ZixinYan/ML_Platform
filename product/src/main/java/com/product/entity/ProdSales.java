package com.product.entity;

import lombok.Data;

@Data
public class ProdSales {

    /**
     * 商品ID
     */
    private Long prodId;
    /**
     * 销量
     */
    private Integer salesCount;
    /**
     * 销量排行
     */
    private Integer salesRank;
    /**
     * 更新时间
     */
    private Long updateTime;

}
