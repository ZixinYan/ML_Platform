package com.product.service.impl;

import com.product.dao.FlashSaleProdInfoDao;
import com.product.entity.FlashSaleProdInfo;
import com.product.service.FlashSaleProdInfoService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class FlashSaleProdInfoServiceImpl extends BaseProdInfoServiceImpl<FlashSaleProdInfoDao, FlashSaleProdInfo> implements FlashSaleProdInfoService {

    @Override
    protected FlashSaleProdInfo createProdInstance() {
        return new FlashSaleProdInfo();
    }

    @Override
    protected void setSpecialFields(FlashSaleProdInfo prodInfo, Map<String, Object> prodMap) {
        prodInfo.setStartTime((Long) prodMap.get("startTime"));
        prodInfo.setEndTime((Long) prodMap.get("endTime"));
        prodInfo.setStock(prodMap.get("stock") != null ? Integer.valueOf(prodMap.get("stock").toString()): null);
    }

    @Override
    protected void updateSpecialFields(FlashSaleProdInfo prodInfo, Map<String, Object> updateMap) {
        if (updateMap.containsKey("startTime")) {
            prodInfo.setStartTime((Long) updateMap.get("startTime"));
        }
        if (updateMap.containsKey("endTime")) {
            prodInfo.setEndTime((Long) updateMap.get("endTime"));
        }
        if (updateMap.containsKey("stock")) {
            prodInfo.setStock(Integer.valueOf(updateMap.get("stock").toString()));
        }
    }

    @Override
    protected void addSpecialFieldsToMap(FlashSaleProdInfo prodInfo, Map<String, Object> map) {
        map.put("startTime", prodInfo.getStartTime());
        map.put("endTime", prodInfo.getEndTime());
        map.put("stock", prodInfo.getStock());
    }
}
