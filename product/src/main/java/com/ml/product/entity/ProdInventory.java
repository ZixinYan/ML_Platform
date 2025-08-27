package com.ml.product.entity;

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

}
