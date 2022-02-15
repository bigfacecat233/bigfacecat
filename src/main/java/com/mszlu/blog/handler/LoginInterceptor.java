package com.mszlu.blog.handler;

import com.alibaba.fastjson.JSON;
import com.mszlu.blog.dao.pojo.SysUser;
import com.mszlu.blog.service.LoginService;
import com.mszlu.blog.utils.UserThreadLocal;
import com.mszlu.blog.vo.ErrorCode;
import com.mszlu.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行controller方法(Handler)之前进行执行
        /*
         * 1．需要判断请求的接口路径是否为 HandlerMethod (controller方法)*
         * 2．判断token是否为空，如果为空未登录
         *3．如果token不为空，登录验证loginservice checkToken
         *4.加果认证成功放行即可
         */
        if (!(handler instanceof HandlerMethod)){
            //handler 可能是RequestResourceHandler springboot程序访问静态资源默认去classpath下的static目录去查询
            return true;
        }
        String token = request.getHeader("Authorization");
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if (token == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            //            浏览器识别返回的是json
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }


        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));//把result转成json
            return false;
        }
        //是登录状态，放行
        //我希望在controller中直接获取用户的信息﹑怎么获取?
        //同时在每次拦截放行之后，说明是有用户存在，并且访问的是要登陆才能访问的资源，此时将查询出的用户放到UserThreadLocal中
        UserThreadLocal.put(sysUser);
        return true;
    }
    // 如果不删除ThreadLocal中用完的信息，会有内存泄露的风险
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}