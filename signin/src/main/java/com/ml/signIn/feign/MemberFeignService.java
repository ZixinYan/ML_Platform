package com.ml.signIn.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "member", contextId = "memberFeignService")
public interface MemberFeignService {

}
