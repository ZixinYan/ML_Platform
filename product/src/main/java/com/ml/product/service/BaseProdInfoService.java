package com.ml.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.product.entity.ProdInfo;
import java.util.Map;
import java.util.List;

public interface BaseProdInfoService<T extends ProdInfo> extends IService<T> {
    // 通用创建方法
    void createProd(Map<String, Object> prodMap);
    // 批量创建方法
    void createProdBatch(List<Map<String, Object>> prodMapList);
    // 通用更新方法
    void updateProd(Map<String, Object> updateMap);
    //更新商品状态（内部接口）
    void updateProdStatus(Long id, Integer status);
    //查询商品名称（内部接口）
    String selectProdNameById(Long id);
    //查询商品状态（内部接口）
    Integer selectProdStatusById(Long id);
    // 通用删除方法
    void deleteProd(Long id);
    //批量删除方法
    void deleteProdBatch(List<Long> ids);
    // 查询全部方法
    String listProdAll(Long ownerId);
    // 通用查询方法
    String selectProdById(Long id,Long ownerId);
    // 批量查询方法
    String selectProdBatchByIds(List<Long> ids,Long ownerId);
}