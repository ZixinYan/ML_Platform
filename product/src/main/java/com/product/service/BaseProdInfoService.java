package com.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.product.entity.ProdInfo;
import java.util.Map;
import java.util.List;

public interface BaseProdInfoService<T extends ProdInfo> extends IService<T> {
    // 通用创建方法
    void createProd(Map<String, Object> prodMap);
    // 批量创建方法
    void createProdBatch(Map<String, Object> prodMap);
    // 通用更新方法
    void updateProd(Map<String, Object> updateMap);
    // 通用删除方法
    void deleteProd(Long id);
    // 查询全部方法
    List<Map<String, Object>> listProdAll();
    // 通用查询方法
    T selectProdById(Long id);
}