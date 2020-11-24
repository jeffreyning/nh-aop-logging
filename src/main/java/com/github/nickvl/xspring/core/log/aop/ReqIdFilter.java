package com.github.nickvl.xspring.core.log.aop;


import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ReqIdFilter implements Filter {

    //add by ninghao
    private UserInfoLog userInfoLog;

    public UserInfoLog getUserInfoLog() {
        return userInfoLog;
    }

    public void setUserInfoLog(UserInfoLog userInfoLog) {
        this.userInfoLog = userInfoLog;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)request;

        String reqId = httpServletRequest.getHeader("Req-Id");
        if(reqId==null || "".equals(reqId)){
            reqId = httpServletRequest.getParameter("req_id");
        }
        if(reqId==null || "".equals(reqId)) {
            reqId = ObjectId.get().toHexString();
        }
        MDC.put("reqId", reqId);
        //add by ninghao
        if(userInfoLog!=null){
            String userId=userInfoLog.getUserId();
            MDC.put("userId", userId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }

    }

    @Override
    public void destroy() {

    }
}