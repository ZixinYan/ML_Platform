package com.ml.authserver.feign;


import com.ml.authserver.vo.*;
import com.ml.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "member",contextId = "memberFeignService")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R regist(@RequestBody UserRegisterVo userRegisterVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R weiboLogin(@RequestBody WeiboUser vo) throws Exception;

    @PostMapping(value = "/member/member/weixin/login")
    R weixinLogin(@RequestBody WxUser vo) throws Exception;

    @PostMapping(value = "member/member/github/login")
    R githubLogin(@RequestBody GithubUser vo) throws Exception;
}
