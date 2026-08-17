package com.jachwisunbae.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.jachwisunbae.auth.token.RefreshToken;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByHashForUpdate(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    int revoke(Long id, LocalDateTime revokedAt);

    int revokeAllActiveByMemberId(Long memberId, LocalDateTime revokedAt);
}
