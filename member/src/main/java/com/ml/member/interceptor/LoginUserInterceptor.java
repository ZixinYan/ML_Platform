package com.ml.member.interceptor;

import com.alibaba.fastjson.JSON;
import com.ml.common.constant.AuthServerConstant;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
@Slf4j
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Session
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("Session is missing!");
            // 用户未登录，返回 401 错误
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(R.error(401, "请先登录")));
            response.getWriter().flush();
            return false; // 拦截请求
        } else {
            log.info("Session ID: {}", session.getId());
            MemberRespVo user = (MemberRespVo) session.getAttribute(AuthServerConstant.SESSION_LOGIN_KEY);
            if (user != null) {
                loginUser.set(user); // 设置到 ThreadLocal 中
                return true; // 用户已登录，放行请求
            }else{
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JSON.toJSONString(R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg())));
                response.getWriter().flush();
                return false;
            }
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除 ThreadLocal，防止内存泄漏
        loginUser.remove();
    }
}

