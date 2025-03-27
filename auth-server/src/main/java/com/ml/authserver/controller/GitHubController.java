package com.ml.authserver.controller;
import com.alibaba.fastjson.JSON;
import com.ml.authserver.feign.MemberFeignService;
import com.ml.authserver.utils.IPUtils;
import com.ml.authserver.vo.GithubUser;
import com.ml.authserver.vo.MemberRespVo;
import com.ml.common.constant.AuthServerConstant;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/github")
public class GitHubController {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${github.clientID}")
    private String clientID;

    @Value("${github.clientSecret}")
    private String clientSecret;

    @Autowired
    private ValueOperations<String, String> valueOperations;

    // 防刷：使用 Redis 存储短时间内的请求标识
    private static final int THROTTLE_TIME_LIMIT = 5; // 5秒内最大请求次数
    private static final int MAX_REQUESTS_PER_IP = 3; // 每个 IP 地址最多 3 次请求

    @GetMapping("/success")
    public R githubLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        String ip = IPUtils.getClientIp();
        String requestKey = "github:login:" + ip;
        String requestCountStr = valueOperations.get(requestKey);
        int requestCount = StringUtils.isEmpty(requestCountStr) ? 0 : Integer.parseInt(requestCountStr);

        if (requestCount >= MAX_REQUESTS_PER_IP) {
            return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), "请求过于频繁，请稍后再试");
        }

        HttpClient httpClient = HttpClientBuilder.create().build();
        String tokenUrl = "https://github.com/login/oauth/access_token?client_id=" + clientID +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=http://localhost:9000/github/success";

        HttpPost tokenRequest = new HttpPost(tokenUrl);
        tokenRequest.setHeader("Accept", "application/json");
        HttpResponse tokenResponse = httpClient.execute(tokenRequest);

        if (tokenResponse.getStatusLine().getStatusCode() != 200) {
            return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), "GitHub 认证失败，无法获取 access_token");
        }

        String tokenJson = EntityUtils.toString(tokenResponse.getEntity());
        Map<String, String> tokenMap = JSON.parseObject(tokenJson, HashMap.class);
        String accessToken = tokenMap.get("access_token");
        log.info(accessToken);
        if(accessToken == null || !accessToken.equals(tokenMap.get("access_token"))){
            return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), "GitHub 认证失败，无法获取 access_token");
        }
        // 将 access_token 存储到 Redis，设置过期时间（例如：1小时）
        String tokenKey = "github:access_token:" + accessToken;
        valueOperations.set(tokenKey, accessToken, 1, TimeUnit.HOURS);

        GithubUser githubUser = new GithubUser();
        githubUser.setAccess_token(accessToken);
        githubUser.setIP(ip);
        R r = memberFeignService.githubLogin(githubUser);

        if (r.getCode() == 0) {
            MemberRespVo memberRespVo = JSON.parseObject(JSON.toJSONString(r.getData()), MemberRespVo.class);
            log.info("GitHub 登录成功，用户信息：{}", memberRespVo);

            session.setAttribute(AuthServerConstant.SESSION_LOGIN_KEY, memberRespVo);

            // 防刷：更新请求计数
            valueOperations.increment(requestKey, 1);
            stringRedisTemplate.expire(requestKey, THROTTLE_TIME_LIMIT, TimeUnit.SECONDS); // 设置过期时间

            return R.ok();
        } else {
            return R.error(r.getCode(), r.getMsg());
        }
    }
}
