package com.product.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.entity.ProdInfo;
import com.product.service.BaseProdInfoService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseProdInfoServiceImpl<M extends BaseMapper<T>, T extends ProdInfo> extends ServiceImpl<M, T> implements BaseProdInfoService<T> {

    @Autowired
    protected M baseProdInfoMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void createProd(Map<String, Object> prodMap) {
        T prodInfo = createProdInstance();
        // 处理通用属性
        setCommonFields(prodInfo, prodMap);
        // 钩子方法：处理子类特有属性
        setSpecialFields(prodInfo, prodMap);
        // 设置时间戳
        long currentTime = System.currentTimeMillis();
        prodInfo.setCreateTime(currentTime);
        prodInfo.setUpdateTime(currentTime);
        // 保存商品
        this.save(prodInfo);
    }

    @Override
    public void createProdBatch(Map<String, Object> prodMap){
        List<Map<String, Object>> prodMaps = (List<Map<String, Object>>) prodMap;
        prodMaps.forEach(this::createProd);
    }

    @Override
    public void updateProd(Map<String, Object> updateMap) {
        Long prodId = updateMap.get("prodId") != null ? Long.valueOf(updateMap.get("prodId").toString()) : null;
        if (prodId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        T prodInfo = this.getById(prodId);
        if (prodInfo == null) {
            throw new RuntimeException("未找到该商品");
        }

        // 更新通用属性
        updateCommonFields(prodInfo, updateMap);
        // 钩子方法：更新子类特有属性
        updateSpecialFields(prodInfo, updateMap);

        prodInfo.setUpdateTime(System.currentTimeMillis());
        this.updateById(prodInfo);
    }

    @Override
    public void deleteProd(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        // 逻辑删除商品
        T prodInfo = this.getById(id);
        if (prodInfo != null) {
            prodInfo.setStatus(0); // 设置为已删除状态
            prodInfo.setUpdateTime(System.currentTimeMillis());
            this.updateById(prodInfo);
        }
    }

    @Override
    public String listProdAll(){
        // 查询所有未删除的商品
        String allProdJson;
        try {
            allProdJson = objectMapper.writeValueAsString(baseProdInfoMapper.selectList(null).stream().filter(prod -> prod.getStatus() != 0).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new RuntimeException("序列化商品信息失败");
        }
        return allProdJson;
    }


    @Override
    public String selectProdById(Long id) {
        // 根据ID查询商品
        T prodInfo = this.getById(id);
        if (prodInfo == null) {
            throw new RuntimeException("未找到该商品");
        }
        String prodInfoJson;
        try {
            prodInfoJson = objectMapper.writeValueAsString(prodInfo);
        } catch (Exception e) {
            throw new RuntimeException("序列化商品信息失败");
        }
        return prodInfoJson;
    }

    @Override
    public String selectProdBatchByIds(List<Long> ids) {
        // 根据ID列表查询商品，并转换为JSON字符串
        String prodInfoJson;
        try {
            prodInfoJson = objectMapper.writeValueAsString(baseProdInfoMapper.selectBatchIds(ids));
        } catch (Exception e) {
            throw new RuntimeException("序列化商品信息失败");
        }
        return prodInfoJson;
    }

    // 处理通用属性的方法
    private void setCommonFields(T prodInfo, Map<String, Object> prodMap) {
        prodInfo.setProdName((String) prodMap.get("prodName"));
        prodInfo.setProdShortDescription((String) prodMap.get("prodShortDescription"));
        prodInfo.setProdPrice(prodMap.get("prodPrice") != null ? Double.valueOf(prodMap.get("prodPrice").toString()) : 0.0);
        prodInfo.setDiscount(prodMap.get("discount") != null ? Double.valueOf(prodMap.get("discount").toString()) : 1.0);
        prodInfo.setProdDiscountPrice(prodInfo.getProdPrice() * prodInfo.getDiscount());
        prodInfo.setImageUrl((String) prodMap.get("imageUrl"));
        prodInfo.setLabel((String) prodMap.get("label"));
        prodInfo.setStatus(2); // 默认上架状态
        prodInfo.setProdDetail((String) prodMap.get("prodDetail"));
        prodInfo.setOwnerId(prodMap.get("ownerId") != null ? Long.valueOf(prodMap.get("ownerId").toString()) : 0L);
    }

    // 更新通用属性的方法
    private void updateCommonFields(T prodInfo, Map<String, Object> updateMap) {
        if (updateMap.containsKey("prodName")) {
            prodInfo.setProdName((String) updateMap.get("prodName"));
        }
        if (updateMap.containsKey("prodShortDescription")) {
            prodInfo.setProdShortDescription((String) updateMap.get("prodShortDescription"));
        }
        if (updateMap.containsKey("prodPrice")) {
            prodInfo.setProdPrice(Double.valueOf(updateMap.get("prodPrice").toString()));
            // 如果价格改变，重新计算折扣价
            prodInfo.setProdDiscountPrice(prodInfo.getProdPrice() * prodInfo.getDiscount());
        }
        if (updateMap.containsKey("discount")) {
            prodInfo.setDiscount(Double.valueOf(updateMap.get("discount").toString()));
            // 如果折扣改变，重新计算折扣价
            prodInfo.setProdDiscountPrice(prodInfo.getProdPrice() * prodInfo.getDiscount());
        }
        if (updateMap.containsKey("imageUrl")) {
            prodInfo.setImageUrl((String) updateMap.get("imageUrl"));
        }
        if (updateMap.containsKey("label")) {
            prodInfo.setLabel((String) updateMap.get("label"));
        }
        if (updateMap.containsKey("status")) {
            prodInfo.setStatus(Integer.valueOf(updateMap.get("status").toString()));
        }
        if (updateMap.containsKey("prodDetail")) {
            prodInfo.setProdDetail((String) updateMap.get("prodDetail"));
        }
    }

    // 转换实体为Map的通用方法(原本是将list数据中的对象转map类型，后来改为转json字符串，此方法暂时弃用)
    protected Map<String, Object> convertToMap(T prod) {
        Map<String, Object> map = new HashMap<>();
        map.put("Id", prod.getId());
        map.put("prodName", prod.getProdName());
        map.put("prodShortDescription", prod.getProdShortDescription());
        map.put("prodPrice", prod.getProdPrice());
        map.put("discount", prod.getDiscount());
        map.put("prodDiscountPrice", prod.getProdDiscountPrice());
        map.put("imageUrl", prod.getImageUrl());
        map.put("label", prod.getLabel());
        map.put("status", prod.getStatus());
        map.put("createTime", prod.getCreateTime());
        map.put("updateTime", prod.getUpdateTime());
        // 钩子方法：添加子类特有属性到Map
        addSpecialFieldsToMap(prod, map);
        return map;
    }

    // 钩子方法：处理子类特有属性（默认空实现）
    protected void setSpecialFields(T prodInfo, Map<String, Object> prodMap) {
        // 由子类实现
    }

    // 钩子方法：更新子类特有属性（默认空实现）
    protected void updateSpecialFields(T prodInfo, Map<String, Object> updateMap) {
        // 由子类实现
    }

    // 钩子方法：添加子类特有属性到Map（默认空实现）
    protected void addSpecialFieldsToMap(T prod, Map<String, Object> map) {
        // 由子类实现
    }

    // 抽象方法：创建具体实例
    protected abstract T createProdInstance();
}