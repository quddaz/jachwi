package com.jachwisunbae.checklist.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.type.Stage;

@Repository
public class JdbcUserChecklistRepository implements UserChecklistRepository {

    private static final RowMapper<UserChecklist> CHECKLIST_MAPPER = (rs, rowNumber) ->
            UserChecklist.restore(
                    rs.getLong("id"),
                    rs.getLong("member_id"),
                    rs.getString("name"),
                    Stage.valueOf(rs.getString("stage")),
                    rs.getTimestamp("deleted_at") == null
                            ? null
                            : rs.getTimestamp("deleted_at").toLocalDateTime());

    private static final RowMapper<UserChecklistItem> ITEM_MAPPER = (rs, rowNumber) ->
            UserChecklistItem.restore(
                    rs.getLong("id"),
                    rs.getLong("user_checklist_id"),
                    rs.getLong("system_check_item_id"),
                    rs.getInt("display_order"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserChecklistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserChecklist save(UserChecklist checklist) {
        if (checklist.getId() == null) {
            return insert(checklist);
        }
        jdbcTemplate.update(
                "UPDATE user_checklists SET name = ? WHERE id = ? AND deleted_at IS NULL",
                checklist.getName(),
                checklist.getId());
        return checklist;
    }

    @Override
    public Optional<UserChecklist> findActiveByIdAndMemberId(Long checklistId, Long memberId) {
        return jdbcTemplate.query("""
                SELECT id, member_id, name, stage, deleted_at
                FROM user_checklists
                WHERE id = ? AND member_id = ? AND deleted_at IS NULL
                """, CHECKLIST_MAPPER, checklistId, memberId).stream().findFirst();
    }

    @Override
    public Optional<UserChecklist> findActiveByIdAndMemberIdForUpdate(Long checklistId, Long memberId) {
        return jdbcTemplate.query("""
                SELECT id, member_id, name, stage, deleted_at
                FROM user_checklists
                WHERE id = ? AND member_id = ? AND deleted_at IS NULL
                FOR UPDATE
                """, CHECKLIST_MAPPER, checklistId, memberId).stream().findFirst();
    }

    @Override
    public List<UserChecklist> findAllActiveByMemberId(Long memberId, Stage stage) {
        if (stage == null) {
            return jdbcTemplate.query("""
                    SELECT id, member_id, name, stage, deleted_at
                    FROM user_checklists
                    WHERE member_id = ? AND deleted_at IS NULL
                    ORDER BY updated_at DESC, id DESC
                    """, CHECKLIST_MAPPER, memberId);
        }
        return jdbcTemplate.query("""
                SELECT id, member_id, name, stage, deleted_at
                FROM user_checklists
                WHERE member_id = ? AND stage = ? AND deleted_at IS NULL
                ORDER BY updated_at DESC, id DESC
                """, CHECKLIST_MAPPER, memberId, stage.name());
    }

    @Override
    public List<UserChecklistItem> findItems(Long checklistId) {
        return jdbcTemplate.query("""
                SELECT id, user_checklist_id, system_check_item_id, display_order
                FROM user_checklist_items
                WHERE user_checklist_id = ?
                ORDER BY display_order
                """, ITEM_MAPPER, checklistId);
    }

    @Override
    public void replaceItems(Long checklistId, List<Long> systemCheckItemIds) {
        jdbcTemplate.update("DELETE FROM user_checklist_items WHERE user_checklist_id = ?", checklistId);
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO user_checklist_items
                            (user_checklist_id, system_check_item_id, display_order)
                        VALUES (?, ?, ?)
                        """,
                systemCheckItemIds,
                systemCheckItemIds.size(),
                (statement, systemItemId) -> {
                    int order = systemCheckItemIds.indexOf(systemItemId) + 1;
                    statement.setLong(1, checklistId);
                    statement.setLong(2, systemItemId);
                    statement.setInt(3, order);
                });
    }

    @Override
    public int countAppliedProperties(Long checklistId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM property_checklists WHERE source_user_checklist_id = ?",
                Integer.class,
                checklistId);
        return count == null ? 0 : count;
    }

    @Override
    public void softDelete(Long checklistId) {
        jdbcTemplate.update(
                "UPDATE user_checklists SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND deleted_at IS NULL",
                checklistId);
    }

    private UserChecklist insert(UserChecklist checklist) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO user_checklists (member_id, name, stage)
                    VALUES (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, checklist.getMemberId());
            statement.setString(2, checklist.getName());
            statement.setString(3, checklist.getStage().name());
            return statement;
        }, keyHolder);
        return UserChecklist.restore(
                keyHolder.getKey().longValue(),
                checklist.getMemberId(),
                checklist.getName(),
                checklist.getStage(),
                null);
    }
}
