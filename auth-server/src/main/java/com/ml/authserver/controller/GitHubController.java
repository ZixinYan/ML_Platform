package com.ml.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.ml.authserver.feign.MemberFeignService;
import com.ml.authserver.vo.GithubUser;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 处理 GitHub OAuth2 登录请求
 */
@Slf4j
@RestController
@RequestMapping("/github")
public class GitHubController {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/success")
    public R githubLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();

        // 1️⃣ **用 code 换 access_token**
        String tokenUrl = "https://github.com/login/oauth/access_token?client_id=your_client_id" +
                "&client_secret=your_client_secret" +
                "&code=" + code +
                "&redirect_uri=http://auth.yoursite.com/oauth2.0/github/success";

        HttpPost tokenRequest = new HttpPost(tokenUrl);
        tokenRequest.setHeader("Accept", "application/json");
        HttpResponse tokenResponse = httpClient.execute(tokenRequest);

        if (tokenResponse.getStatusLine().getStatusCode() != 200) {
            return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), "GitHub 认证失败，无法获取 access_token");
        }

        String tokenJson = EntityUtils.toString(tokenResponse.getEntity());
        Map<String, String> tokenMap = JSON.parseObject(tokenJson, HashMap.class);
        String accessToken = tokenMap.get("access_token");
        GithubUser githubUser = new GithubUser();
        githubUser.setAccess_token(accessToken);
        R r = memberFeignService.githubLogin(githubUser);
        if (r.getCode() == 0) {
            MemberRespVo memberRespVo = JSON.parseObject(JSON.toJSONString(r.getData()), MemberRespVo.class);
            log.info("GitHub 登录成功，用户信息：{}", memberRespVo);

            session.setAttribute(AuthServerConstant.SESSION_LOGIN_KEY, memberRespVo);
            return R.ok();
        } else {
            return R.error(r.getCode(), r.getMsg());
        }
    }
}
