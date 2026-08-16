package com.jachwisunbae.auth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.common.exception.BusinessException;

class JwtTokenProviderTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-16T00:00:00Z"), ZoneOffset.UTC);
    private final JwtTokenProvider provider = new JwtTokenProvider(
            "01234567890123456789012345678901",
            "jachwi-sunbae",
            "jachwi-sunbae-api",
            3600,
            clock);

    @Test
    void createsAndParsesMemberId() {
        String token = provider.createAccessToken(10L);

        assertThat(provider.parseMemberId(token)).isEqualTo(10L);
    }

    @Test
    void rejectsModifiedToken() {
        String token = provider.createAccessToken(10L) + "changed";

        assertThatThrownBy(() -> provider.parseMemberId(token))
                .isInstanceOf(BusinessException.class);
    }
}
