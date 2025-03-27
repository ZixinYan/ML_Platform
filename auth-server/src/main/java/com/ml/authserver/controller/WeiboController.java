package com.ml.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.ml.authserver.feign.MemberFeignService;
import com.ml.authserver.vo.WeiboUser;
import com.ml.common.constant.AuthServerConstant;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 处理社交登录请求
 */
@Slf4j
@RestController
@RequestMapping("/weibo")
public class WeiboController {
    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/success")
    public R weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        String url = "https://api.weibo.com/oauth2/access_token?client_id=1411893798&client_secret=6b03671f1d5bd30edcd63f029a38a428&grant_type=authorization_code&redirect_uri=http://auth.gulimall.com/oauth2.0/weibo/success&code=" +code;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = httpClient.execute(httpPost);
        //2、处理
        if (response.getStatusLine().getStatusCode()==200){
            //获取到accessToken
            String json = EntityUtils.toString(response.getEntity());
            log.info(String.valueOf(response.getEntity()));
            WeiboUser weiboUser = JSON.parseObject(json, WeiboUser.class);
            //知道当前是哪个社交用户登录成功
            //1、当前用户如果是第一次进网站，就自动注册进来（为当前社交用户生成一个会员信息账号,以后这个社交账号就对应指定的会员）
            //登录或者注册这个社交用户
            R r = memberFeignService.weiboLogin(weiboUser);
            if (r.getCode()==0){
                MemberRespVo memberRespVo = (MemberRespVo) r.getData();
                log.info("登录成功，用户信息：" + memberRespVo);
                session.setAttribute(AuthServerConstant.SESSION_LOGIN_KEY,memberRespVo);
                //2、登录成功就跳回首页
                return R.ok();
            }else {
                return R.error(r.getCode(),r.getMsg());
            }
        }else{
            return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), BizCodeEnum.THIRD_PARTY_ERROR.getMsg());
        }

    }
}
