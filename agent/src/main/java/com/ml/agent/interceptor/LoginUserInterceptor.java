package com.ml.agent.interceptor;

import com.alibaba.fastjson.JSON;
import com.ml.common.constant.AuthServerConstant;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.common.vo.MemberRespVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@Slf4j
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Session
        HttpSession session = request.getSession(false);
        if (session == null) {
            // 用户未登录，返回 401 错误
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(R.error(401, "请先登录")));
            response.getWriter().flush();
            return false; // 拦截请求
        } else {
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

