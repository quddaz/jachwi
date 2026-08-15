package com.jachwisunbae.member.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.member.entity.Member;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> ROW_MAPPER = (resultSet, rowNumber) ->
            Member.restore(
                    resultSet.getLong("id"),
                    resultSet.getString("subject"),
                    resultSet.getString("email"),
                    resultSet.getString("name"),
                    resultSet.getTimestamp("last_login_at").toLocalDateTime());

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jdbcTemplate.query("""
                SELECT id, subject, email, name, last_login_at
                FROM members
                WHERE id = ?
                """, ROW_MAPPER, id)
                .stream()
                .findFirst();
    }
}
