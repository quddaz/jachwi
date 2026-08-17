package com.jachwisunbae.auth.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jachwisunbae.auth.token.JwtTokenProvider;

import tools.jackson.databind.ObjectMapper;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(
            "test-secret-key-must-be-at-least-32-bytes-long",
            "issuer",
            "audience",
            3600,
            Clock.fixed(Instant.parse("2026-08-17T00:00:00Z"), ZoneOffset.UTC));
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            tokenProvider,
            new AuthenticationErrorWriter(new ObjectMapper()));

    @Test
    void respondsWithCommonUnauthorizedJsonWhenProtectedRequestHasNoToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("ACCESS_TOKEN_INVALID");
    }

    @Test
    void allowsPublicTokenRotationWithoutAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/tokens");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void storesMemberIdForValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");
        request.addHeader("Authorization", "Bearer " + tokenProvider.createAccessToken(17L));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(request.getAttribute(JwtAuthenticationFilter.MEMBER_ID_ATTRIBUTE)).isEqualTo(17L);
    }
}
