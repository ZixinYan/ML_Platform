package com.product.controller;

import com.ml.common.utils.R;
import com.product.service.ProdInfoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/product")
public class prodInfoController {
    @Autowired
    private ProdInfoHandler prodInfoHandler;

    /*
      商品标签label前端字典对应 1-common,2-code,3-flashSale，传回common/code/flashSale
      label进入工厂类处理获得对应service
     */


    /**
     * 创建单个商品信息
     */
    @PostMapping("/createProd")
    public R<String> createProd(@RequestBody String label,@RequestBody Map<String, Object> prodMap) {
        prodInfoHandler.CreateProdInfo(label, prodMap);
        return R.ok();
    }

    /**
     * 批量创建商品信息
     */
    @PostMapping("/createProdBatch")
    public R<String> createProdBatch(@RequestBody String label,@RequestBody List<Map<String, Object>> prodMapList) {
        prodInfoHandler.CreateProdInfoBatch(label, prodMapList);
        return R.ok();
    }

    /**
     * 更新商品信息
     */
    @PostMapping("/updateProd")
    public R<String> updateProd(@RequestBody String label,@RequestBody Map<String, Object> updateMap) {
        prodInfoHandler.UpdateProdInfo(label, updateMap);
        return R.ok();
    }

    /**
     * 删除商品信息
     */
    @PostMapping("/deleteProd")
    public R<String> deleteProd(@RequestBody String label,@RequestBody Long id) {
        prodInfoHandler.DeleteProdInfo(label, id);
        return R.ok();
    }

    /**
     * 批量删除商品信息
     */
    @PostMapping("/deleteProdBatch")
    public R<String> deleteProdBatch(@RequestBody String label,@RequestBody List<Long> ids) {
        prodInfoHandler.DeleteProdBatchInfo(label, ids);
        return R.ok();
    }

    /**
     * 查询全部商品信息
     */
    @PostMapping("/listProdAll")
    public R<String> listProdAll(@RequestBody String label) {
        return R.ok(prodInfoHandler.ListProdInfo(label));
    }

    /**
     * 根据id查询单个商品信息
     */
    @PostMapping("/selectProd")
    public R<String> selectProdById(@RequestBody String label,@RequestBody Long id) {
        return R.ok(prodInfoHandler.SelectProdInfo(label, id));
    }

    /**
     * 根据ids批量查询商品信息
     */
    @PostMapping("/selectProdBatch")
    public R<String> selectProdBatchByIds(@RequestBody String label,@RequestBody List<Long> ids) {
        return R.ok(prodInfoHandler.SelectProdBatchInfo(label, ids));
    }

}
