package com.dorm.interceptor;

import com.dorm.annotation.RequireRole;
import com.dorm.common.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) return true;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        if (token == null || jwtUtils.isTokenExpired(token)) { response.setStatus(401); return false; }
        Claims claims = jwtUtils.parseToken(token);
        request.setAttribute("userId", claims.get("userId", Long.class));
        request.setAttribute("username", claims.get("username", String.class));
        request.setAttribute("role", claims.get("role", Integer.class));

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null) {
            Integer userRole = claims.get("role", Integer.class);
            boolean allowed = Arrays.stream(requireRole.value()).anyMatch(r -> r == userRole);
            if (!allowed) { response.setStatus(403); return false; }
        }
        return true;
    }
}