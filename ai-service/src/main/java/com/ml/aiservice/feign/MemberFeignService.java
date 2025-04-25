package com.ml.aiservice.feign;

import com.ml.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("member")
public interface MemberFeignService {

    @GetMapping("member/integrationchangehistory/list")
    R getIntegration(@RequestParam("memberId") Long memberId);
}
