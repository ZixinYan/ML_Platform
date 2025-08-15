package com.product.entity;

import lombok.Data;

@Data
public abstract class ProdInfo {
    /**
     * 商品ID
     */
    protected Long prodId;
    /**
     * 商品名称
     */
    protected String prodName;
    /**
     * 商品简介
     */
    protected String prodShortDescription;
    /**
     * 商品原价
     */
    protected Double prodPrice;
    /**
     * 商品折扣
     */
    protected Double discount;
    /**
     * 商品折后价格
     */
    protected Double prodDiscountPrice;
    /**
     * 图片链接（预计是json字符串）
     */
    private String ImageUrl;
    /**
     * 商品标签 1-common,2-code,3-flashSale
     */
    private String label;
    /**
     * 商品状态，0表示已删除，1表示已下架，2表示上架中，3表示上架中但库存不足，4表示异常
     */
    protected Integer Status;
    /**
     * 商品详情描述
     */
    protected String prodDetail;
    /**
     * 商品创建时间(timestamp格式)
     */
    protected Long createTime;
    /**
     * 商品更新时间(timestamp格式)
     */
    protected Long updateTime;
    /**
     * 商品所属用户ID(如果是官方的就是管理员识别id，如果是代码之类的用户上传的产品，就是用户id)
     */
    protected Long ownerId;

}
