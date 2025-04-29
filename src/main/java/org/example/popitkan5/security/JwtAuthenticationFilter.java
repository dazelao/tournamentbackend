package org.example.popitkan5.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@NoArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // Добавим логирование для отладки
        System.out.println("JwtAuthenticationFilter - URI: " + request.getRequestURI());
        System.out.println("JwtAuthenticationFilter - Auth header present: " + (authHeader != null));

        // Пропускаем OPTIONS запросы без проверки токена (для CORS preflight)
        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            System.out.println("Preflight OPTIONS request - authentication skipped");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Проверяем, является ли запрос публичным (регистрация, вход, корневые пути и т.д.)
        String uri = request.getRequestURI();
        if (uri.equals("/") || 
            uri.equals("/favicon.ico") ||
            uri.equals("/api/auth/login") ||
            uri.equals("/api/auth/register") ||
            uri.startsWith("/api/public") || 
            uri.startsWith("/api/simple") || 
            uri.startsWith("/api/test/") ||
            uri.equals("/api-docs.html") ||
            uri.equals("/swagger-ui.html")) {
            System.out.println("Public endpoint - authentication skipped: " + uri);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Если заголовок отсутствует или не начинается с Bearer, пропускаем запрос без аутентификации
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtTokenProvider.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки валидации токена
        }
        
        filterChain.doFilter(request, response);
    }
}
