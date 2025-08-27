package com.ml.product.service.impl;

import com.ml.product.dao.CommonProdInfoDao;
import com.ml.product.entity.CommonProdInfo;
import com.ml.product.service.CommonProdInfoService;
import org.springframework.stereotype.Service;

@Service
public class CommonProdInfoServiceImpl extends BaseProdInfoServiceImpl<CommonProdInfoDao,CommonProdInfo> implements CommonProdInfoService {

    @Override
    protected CommonProdInfo createProdInstance() {
        return new CommonProdInfo();
    }

    // 由于没有特有属性，不需要覆盖其他钩子方法

}
