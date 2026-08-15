package com.jachwisunbae.checklist.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

@Repository
public class JdbcSystemCheckItemRepository implements SystemCheckItemRepository {

    private static final RowMapper<SystemCheckItem> ROW_MAPPER = (resultSet, rowNumber) ->
            SystemCheckItem.restore(
                    resultSet.getLong("id"),
                    Stage.valueOf(resultSet.getString("stage")),
                    ItemType.valueOf(resultSet.getString("item_type")),
                    resultSet.getString("question"),
                    resultSet.getString("guide"),
                    resultSet.getBoolean("is_active"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcSystemCheckItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SystemCheckItem save(SystemCheckItem item) {
        if (item.getId() == null) {
            return insert(item);
        }

        jdbcTemplate.update(
                """
                        UPDATE system_check_items
                        SET stage = ?, item_type = ?, question = ?, guide = ?, is_active = ?
                        WHERE id = ?
                        """,
                item.getStage().name(),
                item.getItemType().name(),
                item.getQuestion(),
                item.getGuide(),
                item.isActive(),
                item.getId());
        return item;
    }

    @Override
    public Optional<SystemCheckItem> findById(Long id) {
        return jdbcTemplate.query("""
                SELECT id, stage, item_type, question, guide, is_active
                FROM system_check_items
                WHERE id = ?
                """, ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    public List<SystemCheckItem> findAllByStageAndActiveTrue(Stage stage) {
        return jdbcTemplate.query("""
                SELECT id, stage, item_type, question, guide, is_active
                FROM system_check_items
                WHERE stage = ? AND is_active = TRUE
                ORDER BY CASE item_type WHEN 'CORE' THEN 0 ELSE 1 END, id
                """, ROW_MAPPER, stage.name());
    }

    @Override
    public List<SystemCheckItem> findActive(
            Stage stage,
            ItemType itemType,
            String query) {
        boolean hasType = itemType != null;
        boolean hasQuery = query != null;

        return jdbcTemplate.query(
                """
                        SELECT id, stage, item_type, question, guide, is_active
                        FROM system_check_items
                        WHERE stage = ?
                          AND is_active = TRUE
                          AND (? = FALSE OR item_type = ?)
                          AND (? = FALSE OR question LIKE CONCAT('%', ?, '%'))
                        ORDER BY CASE item_type WHEN 'CORE' THEN 0 ELSE 1 END, id
                        """,
                ROW_MAPPER,
                stage.name(),
                hasType,
                hasType ? itemType.name() : "",
                hasQuery,
                hasQuery ? query : "");
    }

    @Override
    public List<SystemCheckItem> findActiveCoreByStage(Stage stage) {
        return jdbcTemplate.query("""
                SELECT id, stage, item_type, question, guide, is_active
                FROM system_check_items
                WHERE stage = ? AND item_type = 'CORE' AND is_active = TRUE
                ORDER BY id
                """, ROW_MAPPER, stage.name());
    }

    @Override
    public List<SystemCheckItem> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = """
                SELECT id, stage, item_type, question, guide, is_active
                FROM system_check_items
                WHERE id IN (%s)
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, ROW_MAPPER, ids.toArray());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM system_check_items WHERE id = ?", id);
    }

    private SystemCheckItem insert(SystemCheckItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO system_check_items
                                (stage, item_type, question, guide, is_active)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, item.getStage().name());
            statement.setString(2, item.getItemType().name());
            statement.setString(3, item.getQuestion());
            statement.setString(4, item.getGuide());
            statement.setBoolean(5, item.isActive());
            return statement;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return SystemCheckItem.restore(
                id,
                item.getStage(),
                item.getItemType(),
                item.getQuestion(),
                item.getGuide(),
                item.isActive());
    }
}
