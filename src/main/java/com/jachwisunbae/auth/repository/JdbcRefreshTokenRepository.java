package com.jachwisunbae.auth.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.auth.token.RefreshToken;

@Repository
public class JdbcRefreshTokenRepository implements RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRefreshTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RefreshToken> findByHashForUpdate(String tokenHash) {
        List<RefreshToken> tokens = jdbcTemplate.query("""
                SELECT id, member_id, token_hash, expires_at, revoked_at
                FROM refresh_tokens
                WHERE token_hash = ?
                FOR UPDATE
                """, this::mapRefreshToken, tokenHash);
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tokens.getFirst());
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO refresh_tokens (member_id, token_hash, expires_at)
                    VALUES (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, refreshToken.getMemberId());
            statement.setString(2, refreshToken.getTokenHash());
            statement.setObject(3, refreshToken.getExpiresAt());
            return statement;
        }, keys);
        return RefreshToken.restore(
                keys.getKey().longValue(),
                refreshToken.getMemberId(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt(),
                null);
    }

    @Override
    public int revoke(Long id, LocalDateTime revokedAt) {
        return jdbcTemplate.update("""
                UPDATE refresh_tokens SET revoked_at = ?
                WHERE id = ? AND revoked_at IS NULL
                """, revokedAt, id);
    }

    @Override
    public int revokeAllActiveByMemberId(Long memberId, LocalDateTime revokedAt) {
        return jdbcTemplate.update("""
                UPDATE refresh_tokens SET revoked_at = ?
                WHERE member_id = ? AND revoked_at IS NULL
                """, revokedAt, memberId);
    }

    private RefreshToken mapRefreshToken(ResultSet resultSet, int rowNumber) throws SQLException {
        LocalDateTime revokedAt = resultSet.getTimestamp("revoked_at") == null
                ? null
                : resultSet.getTimestamp("revoked_at").toLocalDateTime();
        return RefreshToken.restore(
                resultSet.getLong("id"),
                resultSet.getLong("member_id"),
                resultSet.getString("token_hash"),
                resultSet.getTimestamp("expires_at").toLocalDateTime(),
                revokedAt);
    }
}
