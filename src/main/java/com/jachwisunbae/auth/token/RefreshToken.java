package com.jachwisunbae.auth.token;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class RefreshToken {

    private final Long id;
    private final Long memberId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private final LocalDateTime revokedAt;

    private RefreshToken(
            Long id,
            Long memberId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt) {
        this.id = id;
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public static RefreshToken issue(Long memberId, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshToken(null, memberId, tokenHash, expiresAt, null);
    }

    public static RefreshToken restore(
            Long id,
            Long memberId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt) {
        return new RefreshToken(id, memberId, tokenHash, expiresAt, revokedAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
