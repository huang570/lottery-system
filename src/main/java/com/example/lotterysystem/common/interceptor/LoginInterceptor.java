package com.example.lotterysystem.common.interceptor;

import com.example.lotterysystem.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.logging.Logger;

/**
 * 登录拦截器
 *
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 预处理，业务请求前调用
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头
        String token = request.getHeader("user_token");
        log.info("获取Token: {}",token);
        log.info("获取请求路径: {}",request.getRequestURI());

        // 令牌解析
        Claims claims = JWTUtil.parseJWT(token);
        if(null == claims){
            log.error("解析JWT令牌失败!");
            response.setStatus(401);
            return false;
        }
        log.info("解析JWT令牌成功!");
        return true;
    }
}
