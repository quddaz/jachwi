package com.jachwisunbae.auth.web;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.jachwisunbae.auth.token.JwtTokenProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String MEMBER_ID_ATTRIBUTE = "authenticatedMemberId";
    private final JwtTokenProvider provider;
    public JwtAuthenticationFilter(JwtTokenProvider provider) { this.provider = provider; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            request.setAttribute(MEMBER_ID_ATTRIBUTE, provider.parseMemberId(authorization.substring(7)));
        }
        chain.doFilter(request, response);
    }
}
