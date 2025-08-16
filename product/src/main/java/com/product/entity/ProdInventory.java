package com.product.entity;

import lombok.Data;

@Data
public class ProdInventory {

    /**
     * 商品ID
     */
    private Long prodId;
    /**
     * 商品库存
     */
    private Integer prodStock;
    /**
     * 商品限购数量,0表示不限购
     */
    private Integer prodLimit;

}
