package com.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProdInfoHandler {
    @Autowired
    private CommonProdInfoService commonProdInfoService;
    @Autowired
    private FlashSaleProdInfoService flashSaleProdInfoService;
    @Autowired
    private CodeProdInfoService codeProdInfoService;

    public void CreateProdInfo(String label, Map<String, Object> prodMap) {
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
    }

    public void UpdateProdInfo(String label, Map<String, Object> updateMap) {
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
    }

    public void DeleteProdInfo(String label, Long id) {
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
    }

    public List<Map<String, Object>> ListProdInfo(String label) {
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
    }

}
