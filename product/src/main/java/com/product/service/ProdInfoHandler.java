package com.product.service;

import com.product.factory.ProdInfoFactory;
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

    public void CreateProdInfoBatch(String label, Map<String, Object> prodMap) {
        /**
        switch (label) {
            case "common":
                commonProdInfoService.createProdBatch(prodMap);
                break;
            case "flashSale":
                flashSaleProdInfoService.createProdBatch(prodMap);
                break;
            case "code":
                codeProdInfoService.createProdBatch(prodMap);
                break;
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        prodInfoFactory.getProdInfoService(label).createProdBatch(prodMap);
    }

    public void UpdateProdInfo(String label, Map<String, Object> updateMap) {
       /**
        switch (label) {
            case "common":
                commonProdInfoService.updateProd(updateMap);
                break;
            case "flashSale":
                flashSaleProdInfoService.updateProd(updateMap);
                break;
            case "code":
                codeProdInfoService.updateProd(updateMap);
                break;
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        prodInfoFactory.getProdInfoService(label).updateProd(updateMap);
    }

    public void DeleteProdInfo(String label, Long id) {
        /**
        switch (label) {
            case "common":
                commonProdInfoService.deleteProd(id);
                break;
            case "flashSale":
                flashSaleProdInfoService.deleteProd(id);
                break;
            case "code":
                codeProdInfoService.deleteProd(id);
                break;
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        prodInfoFactory.getProdInfoService(label).deleteProd(id);
    }

    public String ListProdInfo(String label) {
        /**
        switch (label) {
            case "common":
                return commonProdInfoService.listProdAll();
            case "flashSale":
                return flashSaleProdInfoService.listProdAll();
            case "code":
                return codeProdInfoService.listProdAll();
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        return prodInfoFactory.getProdInfoService(label).listProdAll();
    }

    public String SelectProdInfo(String label, Long id) {
        /**
        switch (label) {
            case "common":
                return commonProdInfoService.selectProdById(id);
            case "flashSale":
                return flashSaleProdInfoService.selectProdById(id);
            case "code":
                return codeProdInfoService.selectProdById(id);
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        return prodInfoFactory.getProdInfoService(label).selectProdById(id);
    }

    public String SelectProdBatchInfo(String label, List<Long> ids) {
        /**
        switch (label) {
            case "common":
                return commonProdInfoService.selectProdBatchByIds(ids);
            case "flashSale":
                return flashSaleProdInfoService.selectProdBatchByIds(ids);
            case "code":
                return codeProdInfoService.selectProdBatchByIds(ids);
            default:
                throw new IllegalArgumentException("产品类型非法");
        }
         */
        return prodInfoFactory.getProdInfoService(label).selectProdBatchByIds(ids);
    }



}
