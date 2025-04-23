package com.ml.authserver.feign;

import com.ml.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "third-party",contextId = "thirdPartyFeignService")
public interface ThirdPartyFeignService {
    @GetMapping("third-party/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
