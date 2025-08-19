package com.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.product.entity.ProdInfo;
import java.util.Map;
import java.util.List;

public interface BaseProdInfoService<T extends ProdInfo> extends IService<T> {
    // 通用创建方法
    void createProd(Map<String, Object> prodMap);
    // 批量创建方法
    void createProdBatch(List<Map<String, Object>> prodMapList);
    // 通用更新方法
    void updateProd(Map<String, Object> updateMap);
    // 通用删除方法
    void deleteProd(Long id);
    //批量删除方法
    void deleteProdBatch(List<Long> ids);
    // 查询全部方法
    String listProdAll();
    // 通用查询方法
    String selectProdById(Long id);
    // 批量查询方法
    String selectProdBatchByIds(List<Long> ids);
}