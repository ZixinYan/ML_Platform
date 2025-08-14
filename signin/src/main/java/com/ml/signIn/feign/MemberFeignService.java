package com.ml.signIn.feign;

import com.ml.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "member", contextId = "memberFeignService")
public interface MemberFeignService {
    @PostMapping("/member/member/update")
    R update(@RequestBody Map<String, Object> params);
}
