package com.ml.product.service;

import com.ml.product.factory.ProdInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProdInfoHandler {
    @Autowired
    private ProdInfoFactory prodInfoFactory;
    /**
    @Autowired
    private CommonProdInfoService commonProdInfoService;
    @Autowired
    private FlashSaleProdInfoService flashSaleProdInfoService;
    @Autowired
    private CodeProdInfoService codeProdInfoService;
    */

    public void CreateProdInfo(String label, Map<String, Object> prodMap) {
        /**
        switch (label) {
            case "common":
                commonProdInfoService.createProd(prodMap);
                break;
            case "flashSale":
                flashSaleProdInfoService.createProd(prodMap);
                break;
            case "code":
                codeProdInfoService.createProd(prodMap);
                break;
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        prodInfoFactory.getProdInfoService(label).createProd(prodMap);
    }

    public void CreateProdInfoBatch(String label, List<Map<String, Object>> prodMapList) {
        prodInfoFactory.getProdInfoService(label).createProdBatch(prodMapList);
    }

    public void UpdateProdInfo(String label, Map<String, Object> updateMap) {
        prodInfoFactory.getProdInfoService(label).updateProd(updateMap);
    }

    public void DeleteProdInfo(String label, Long id) {
        prodInfoFactory.getProdInfoService(label).deleteProd(id);
    }

    public void DeleteProdBatchInfo(String label, List<Long> ids) {
        prodInfoFactory.getProdInfoService(label).deleteProdBatch(ids);
    }

    public String ListProdInfo(String label,Long ownerId) {
        return prodInfoFactory.getProdInfoService(label).listProdAll(ownerId);
    }

    public String SelectProdInfo(String label, Long id,Long ownerId) {
        return prodInfoFactory.getProdInfoService(label).selectProdById(id,ownerId);
    }

    public String SelectProdBatchInfo(String label, List<Long> ids,Long ownerId) {
        return prodInfoFactory.getProdInfoService(label).selectProdBatchByIds(ids,ownerId);
    }

}
