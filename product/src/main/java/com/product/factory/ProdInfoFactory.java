package com.product.factory;

import com.product.service.BaseProdInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("prodInfoFactory")
public class ProdInfoFactory {

    private final Map<String, BaseProdInfoService> prodInfoServices = new HashMap<>();

    @Autowired
    public ProdInfoFactory(ApplicationContext context) {
        // 动态获取所有实现了BaseProdInfoService接口的bean，并存入map中

        Map<String,BaseProdInfoService> beans = context.getBeansOfType(BaseProdInfoService.class);
        beans.forEach((k, v) -> prodInfoServices.put(v.getClass().getSimpleName(), v));
    }
    public BaseProdInfoService getProdInfoService(String label) {
        BaseProdInfoService service = prodInfoServices.get(processLabel(label));
        if (service == null) {
            throw new IllegalArgumentException("找不到对应的商品信息服务");
        }
        return service;
    }
    public static String processLabel(String label) {
        return capitalize(label)+"ProdInfoService";
    }
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
