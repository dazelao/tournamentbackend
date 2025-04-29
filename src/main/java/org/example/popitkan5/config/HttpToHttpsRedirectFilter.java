package org.example.popitkan5.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpToHttpsRedirectFilter implements Filter {

    @Value("${server.port:8443}")
    private int serverPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Проверяем, использует ли запрос HTTP вместо HTTPS
        // Временно отключим редирект, т.к. nginx уже обрабатывает HTTPS
        if (false && !httpRequest.isSecure() && !"localhost".equals(httpRequest.getServerName())) {
            String redirectUrl = "https://" + httpRequest.getServerName() + ":" + serverPort +
                    httpRequest.getRequestURI() +
                    (httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : "");
            
            httpResponse.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(request, response);
    }
}
