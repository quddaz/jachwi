package com.jachwisunbae.member.repository;

import java.util.Optional;
import java.sql.Statement;
import org.springframework.jdbc.support.GeneratedKeyHolder;

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

    @Override
    public Optional<Member> findBySubject(String subject) {
        return jdbcTemplate.query("""
                SELECT id, subject, email, name, last_login_at FROM members WHERE subject = ?
                """, ROW_MAPPER, subject).stream().findFirst();
    }

    @Override
    public Member save(Member member) {
        if (member.getId() != null) {
            jdbcTemplate.update("""
                    UPDATE members SET email = ?, name = ?, last_login_at = ? WHERE id = ?
                    """, member.getEmail(), member.getName(), member.getLastLoginAt(), member.getId());
            return member;
        }
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO members (subject, email, name, last_login_at) VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, member.getSubject());
            statement.setString(2, member.getEmail());
            statement.setString(3, member.getName());
            statement.setObject(4, member.getLastLoginAt());
            return statement;
        }, keys);
        return Member.restore(keys.getKey().longValue(), member.getSubject(), member.getEmail(),
                member.getName(), member.getLastLoginAt());
    }
}
