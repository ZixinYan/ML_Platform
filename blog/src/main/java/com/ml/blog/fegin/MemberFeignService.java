package com.ml.blog.fegin;

import com.ml.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("member")
public interface MemberFeignService {
    @GetMapping("/member/member/list")
    R getMemberList(@RequestParam("memberIds") List<Long> memberIds);

    @GetMapping("/member/member/info")
    R getUserInfo(@RequestParam("id") Long answerId);
}
