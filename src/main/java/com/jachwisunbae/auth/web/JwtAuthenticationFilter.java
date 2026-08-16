package com.jachwisunbae.auth.web;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jachwisunbae.auth.token.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String MEMBER_ID_ATTRIBUTE = "authenticatedMemberId";
    private final JwtTokenProvider provider;

    public JwtAuthenticationFilter(JwtTokenProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (isBearerToken(authorization)) {
            request.setAttribute(
                    MEMBER_ID_ATTRIBUTE,
                    provider.parseMemberId(extractToken(authorization)));
        }
        chain.doFilter(request, response);
    }

    private boolean isBearerToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ");
    }

    private String extractToken(String authorization) {
        return authorization.substring("Bearer ".length());
    }
}
