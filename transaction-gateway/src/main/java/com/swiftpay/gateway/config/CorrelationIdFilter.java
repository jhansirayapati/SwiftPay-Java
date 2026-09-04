package com.swiftpay.gateway.config;

import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.UUID;
import org.springframework.stereotype.Component;

@Component public class CorrelationIdFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http=(HttpServletRequest)request; HttpServletResponse out=(HttpServletResponse)response;
        String id=http.getHeader("X-Correlation-ID"); if(id==null||id.isBlank()) id=UUID.randomUUID().toString(); out.setHeader("X-Correlation-ID",id); chain.doFilter(request,response);
    }
}