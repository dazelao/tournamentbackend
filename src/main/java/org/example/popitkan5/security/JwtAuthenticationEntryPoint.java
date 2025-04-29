package org.example.popitkan5.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.popitkan5.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // Проверим не является ли запрос OPTIONS для CORS preflight
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, x-auth-token, *");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            return;
        }
        
        // Проверим, является ли этот URL публичным
        String uri = request.getRequestURI();
        if (uri.equals("/") || uri.equals("/favicon.ico")) {
            // Для корневого пути и favicon.ico перенаправляем на индексную страницу без ошибки
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Добавим логирование для отладки
        System.out.println("JwtAuthenticationEntryPoint triggered for: " + uri);
        System.out.println("Authentication error: " + authException.getMessage());
        
        // Проверим, является ли этот URL публичным API
        if (uri.startsWith("/api/auth/") || uri.contains("/register") || uri.contains("/login")) {
            System.out.println("WARNING: Authentication failing on public endpoint: " + uri);
        }
        
        // Добавим CORS заголовки для ответа об ошибке
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Несанкціонований доступ")
                .message("Необхідна аутентифікація - " + authException.getMessage())
                .path(request.getRequestURI())
                .build();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Для поддержки Java 8 Date/Time API
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
