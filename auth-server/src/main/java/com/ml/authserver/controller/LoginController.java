package com.ml.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.authserver.feign.MemberFeignService;
import com.ml.authserver.feign.ThirdPartyFeignService;
import com.ml.authserver.utils.IPUtils;
import com.ml.authserver.vo.UserLoginVo;
import com.ml.authserver.vo.UserRegisterVo;
import com.ml.common.constant.AuthServerConstant;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.common.vo.MemberRespVo;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private ThirdPartyFeignService feignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private ObjectMapper objectMapper;
    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        //1、接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(redisCode)){
            //redis存的时间戳
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000){
                //1分钟内已给这个手机号发过短信，不能在发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //2、验证码再次校验，存redis ,key=phone,value=code
        SecureRandom random = new SecureRandom();
        int codeNum = 10000 + random.nextInt(90000); // 生成 [10000, 99999] 之间的随机数
        String code = String.valueOf(codeNum);
        log.info(code);
        //redis中存储的验证码+时间戳
        String redisValue = code +"_"+System.currentTimeMillis();
        //redis缓存验证码，防止同一个手机号再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,redisValue,10, TimeUnit.MINUTES);
        feignService.sendCode(phone,code);
        return R.ok("验证码成功发送");
    }

    /**
     *
     * @param vo
     * @param result 利用session原理，将数据放在session中，只要跳到下一个页面的取出这个数据以后，session里面的数据就会被删除
     */
    @PostMapping("/register")
    public R register(@Valid UserRegisterVo vo, BindingResult result){
        if (result.hasErrors()) {
            String errorMsg = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            return R.error(BizCodeEnum.REGISTER_ERROR.getCode(), errorMsg);
        }
        //真正注册，调用远程服务注册
        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotBlank(s)){
            if (code.equals(s.split("_")[0])){
                //删除验证码;令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码校验通过,真正注册，调用远程服务注册
                R r = memberFeignService.regist(vo);
                if (r.getCode()==0){
                    //成功,转到登录页
                    return R.ok("注册成功");
                }else{
                    return R.error(r.getCode(), r.getMsg());
                }
            }else {
                //校验出错，转发到注册页
                return  R.error(BizCodeEnum.SMS_CODE_ERROR.getCode(), BizCodeEnum.SMS_CODE_ERROR.getMsg());
            }
        }else{
            //校验出错，转发到注册页
            return R.error(BizCodeEnum.SMS_CODE_ERROR.getCode(), BizCodeEnum.SMS_CODE_ERROR.getMsg());
        }
    }
    @PostMapping("/login")
    public R login(UserLoginVo vo, BindingResult result, HttpSession session){
        //远程登录
        String ip = IPUtils.getClientIp();
        vo.setIP(ip);
        R r = memberFeignService.login(vo);
        if (r.getCode()==0){
            ObjectMapper objectMapper = new ObjectMapper();
            MemberRespVo data = objectMapper.convertValue(r.getData(), MemberRespVo.class);
            session.setAttribute(AuthServerConstant.SESSION_LOGIN_KEY,data);
            log.info(session.getId());
            return R.ok(AuthServerConstant.SESSION_LOGIN_KEY,data);
        }else{
            return R.error(r.getCode(),r.getMsg());
        }
    }
}
