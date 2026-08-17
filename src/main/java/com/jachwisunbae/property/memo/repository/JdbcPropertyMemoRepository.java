package com.jachwisunbae.property.memo.repository;

import java.util.List;
import java.util.Optional;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jachwisunbae.property.memo.entity.PropertyMemo;
import com.jachwisunbae.property.memo.entity.PropertyMemoItem;
import com.jachwisunbae.property.memo.entity.PropertyMemoSnapshot;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemCommand;

@Repository
public class JdbcPropertyMemoRepository implements PropertyMemoRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPropertyMemoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PropertyMemoSnapshot> findByPropertyId(Long propertyId) {
        List<PropertyMemo> memos = jdbcTemplate.query("""
                SELECT id, property_id, free_memo, updated_at
                FROM property_memos
                WHERE property_id = ?
                """, this::mapMemo, propertyId);
        if (memos.isEmpty()) {
            return Optional.empty();
        }
        PropertyMemo memo = memos.getFirst();
        List<PropertyMemoItem> items = jdbcTemplate.query("""
                SELECT id, property_memo_id, label, content, display_order
                FROM property_memo_items
                WHERE property_memo_id = ?
                ORDER BY display_order
                """, this::mapItem, memo.getId());
        return Optional.of(new PropertyMemoSnapshot(memo, items));
    }

    @Override
    public Long saveRoot(Long propertyId, String freeMemo) {
        jdbcTemplate.update("""
                INSERT INTO property_memos (property_id, free_memo)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE free_memo = VALUES(free_memo)
                """, propertyId, freeMemo);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM property_memos WHERE property_id = ?", Long.class, propertyId);
    }

    @Override
    public void deleteItems(Long propertyMemoId) {
        jdbcTemplate.update(
                "DELETE FROM property_memo_items WHERE property_memo_id = ?", propertyMemoId);
    }

    @Override
    public void insertItems(Long propertyMemoId, List<PropertyMemoItemCommand> items) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO property_memo_items (
                    property_memo_id, label, content, display_order
                ) VALUES (?, ?, ?, ?)
                """, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement statement, int index)
                            throws SQLException {
                        PropertyMemoItemCommand item = items.get(index);
                        statement.setLong(1, propertyMemoId);
                        statement.setString(2, item.label());
                        statement.setString(3, item.content());
                        statement.setInt(4, index + 1);
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                });
    }

    private PropertyMemo mapMemo(ResultSet resultSet, int rowNumber) throws SQLException {
        return PropertyMemo.restore(
                resultSet.getLong("id"),
                resultSet.getLong("property_id"),
                resultSet.getString("free_memo"),
                resultSet.getTimestamp("updated_at").toLocalDateTime());
    }

    private PropertyMemoItem mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return PropertyMemoItem.restore(
                resultSet.getLong("id"),
                resultSet.getLong("property_memo_id"),
                resultSet.getString("label"),
                resultSet.getString("content"),
                resultSet.getInt("display_order"));
    }
}
