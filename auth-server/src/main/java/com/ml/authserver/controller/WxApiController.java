package com.ml.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.ml.authserver.feign.MemberFeignService;
import com.ml.authserver.utils.ConstantWxUtils;
import com.ml.authserver.utils.HttpClientUtils;
import com.ml.authserver.vo.WxUser;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.common.vo.MemberRespVo;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import cn.hutool.extra.qrcode.QrCodeUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ml.authserver.utils.WxUtils.checkSignature;
import static com.ml.common.constant.AuthServerConstant.SESSION_LOGIN_KEY;

@Slf4j
@Controller
@RestController
@RequestMapping( "auth/wx")
public class WxApiController {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/check")
    @ResponseBody
    public String check(String signature, String timestamp, String nonce, String echostr) {
        if (checkSignature(signature, timestamp, nonce)) {
            log.info(echostr);
            return echostr;
        }else{
            return null;
        }
    }

    /**
     * 获取扫码人的信息，添加数据
     * @return
     */
    @GetMapping(value = "/callback")
    public R callback(String code, HttpSession session) throws Exception {
        try {
            //得到授权临时票据code
            System.out.println(code);
            String stateKey = "wx-open-state";
            String state = (String) session.getAttribute(stateKey);
            if (state == null || !state.equals(code)) {
                return R.error(BizCodeEnum.THIRD_PARTY_ERROR.getCode(), "非法访问");
            }
            session.removeAttribute(stateKey);
            //2、拿着code请求 微信固定的地址，得到两个 access_token 和 openid
            String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=%s" +
                    "&secret=%s" +
                    "&code=%s" +
                    "&grant_type=authorization_code";

            //拼接三个参数：id 秘钥 和 code 值
            String accessTokenUrl = String.format(
                    baseAccessTokenUrl,
                    ConstantWxUtils.WX_OPEN_APP_ID,
                    ConstantWxUtils.WX_OPEN_APP_SECRET,
                    code
            );
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);
            WxUser wxUser = new WxUser();
            wxUser.setAccessToken(accessTokenInfo);
            R r = memberFeignService.weixinLogin(wxUser);
            if (r.getCode() == 0) {
                MemberRespVo data = (MemberRespVo) r.getData();
                log.info("登录成功：用户信息：{}",data.toString());

                //1、第一次使用session，命令浏览器保存卡号，JSESSIONID这个cookie
                //以后浏览器访问哪个网站就会带上这个网站的cookie
                session.setAttribute(SESSION_LOGIN_KEY,data);

                //2、登录成功跳回首页
                return R.ok();
            } else {

                return R.error(r.getCode(), r.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }

    /**
     * 生成微信扫描二维码
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping(value = "/login")
    public R getWxCode(HttpSession session) throws UnsupportedEncodingException {
        //微信开发平台授权baseUrl   %s相当于？表示占位符
        String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=%s" +
                "&redirect_uri=%s" +
                "&response_type=code" +
                "&scope=snsapi_userinfo" +
                "&state=%s" +
                "#wechat_redirect";

        //对redirect_url进行URLEncoder编码
        String redirect_url = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
        redirect_url = URLEncoder.encode(redirect_url,"UTF-8");
        // 防止csrf攻击（跨站请求伪造攻击）
        String state = UUID.randomUUID().toString().replaceAll("-", "");//一般情况下会使用一个随机数
        session.setAttribute("wx-open-state", state);
        //设置%s中的值
        String url = String.format(
                baseUrl,
                ConstantWxUtils.WX_OPEN_APP_ID,
                redirect_url,
                state
        );
        //重定向到请求微信地址
        return R.ok(url);
    }
}
