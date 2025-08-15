package com.product.service.impl;

import com.product.dao.CodeProdInfoDao;
import com.product.entity.CodeProdInfo;
import com.product.service.CodeProdInfoService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CodeProdInfoServiceImpl extends BaseProdInfoServiceImpl<CodeProdInfoDao, CodeProdInfo> implements CodeProdInfoService {

    @Override
    protected CodeProdInfo createProdInstance() {
        return new CodeProdInfo();
    }

    @Override
    protected void setSpecialFields(CodeProdInfo prodInfo, Map<String, Object> prodMap) {
        prodInfo.setRepositoryId((String) prodMap.get("repositoryId"));
        prodInfo.setLanguage(prodMap.get("language") != null ? Integer.valueOf(prodMap.get("language").toString()) : null);
    }

    @Override
    protected void updateSpecialFields(CodeProdInfo prodInfo, Map<String, Object> updateMap) {
        if (updateMap.containsKey("repositoryId")) {
            prodInfo.setRepositoryId((String) updateMap.get("repositoryId"));
        }
        if (updateMap.containsKey("language")) {
            prodInfo.setLanguage(Integer.valueOf(updateMap.get("language").toString()));
        }
    }

    @Override
    protected void addSpecialFieldsToMap(CodeProdInfo prodInfo, Map<String, Object> map) {
        map.put("repositoryId", prodInfo.getRepositoryId());
        map.put("language", prodInfo.getLanguage());
    }
}