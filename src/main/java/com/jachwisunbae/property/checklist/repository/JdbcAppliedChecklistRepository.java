package com.jachwisunbae.property.checklist.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.entity.AppliedChecklist;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItemDraft;
import com.jachwisunbae.property.checklist.type.CheckStatus;

@Repository
public class JdbcAppliedChecklistRepository implements AppliedChecklistRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAppliedChecklistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<AppliedChecklist> findByPropertyAndStageForUpdate(
            Long propertyId,
            Stage stage) {
        List<AppliedChecklist> checklists = jdbcTemplate.query("""
                SELECT id, property_id, source_user_checklist_id,
                       checklist_name_snapshot, stage
                FROM property_checklists
                WHERE property_id = ? AND stage = ?
                FOR UPDATE
                """, this::mapChecklist, propertyId, stage.name());
        return first(checklists);
    }

    @Override
    public Optional<AppliedChecklist> findOwnedById(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId) {
        List<AppliedChecklist> checklists = jdbcTemplate.query("""
                SELECT pc.id, pc.property_id, pc.source_user_checklist_id,
                       pc.checklist_name_snapshot, pc.stage
                FROM property_checklists pc
                JOIN properties p ON p.id = pc.property_id
                WHERE pc.id = ? AND pc.property_id = ? AND p.member_id = ?
                """, this::mapChecklist, propertyChecklistId, propertyId, memberId);
        return first(checklists);
    }

    @Override
    public List<AppliedChecklist> findAllOwned(Long memberId, Long propertyId) {
        return jdbcTemplate.query("""
                SELECT pc.id, pc.property_id, pc.source_user_checklist_id,
                       pc.checklist_name_snapshot, pc.stage
                FROM property_checklists pc
                JOIN properties p ON p.id = pc.property_id
                WHERE pc.property_id = ? AND p.member_id = ?
                ORDER BY FIELD(pc.stage, 'ONLINE_PHONE', 'ON_SITE', 'PRE_CONTRACT')
                """, this::mapChecklist, propertyId, memberId);
    }

    @Override
    public List<AppliedChecklistItem> findItems(Long propertyChecklistId) {
        return jdbcTemplate.query("""
                SELECT id, property_checklist_id, source_system_check_item_id,
                       question_snapshot, guide_snapshot, display_order, status, memo
                FROM property_checklist_items
                WHERE property_checklist_id = ?
                ORDER BY display_order
                """, this::mapItem, propertyChecklistId);
    }

    @Override
    public AppliedChecklist save(AppliedChecklist checklist) {
        if (checklist.getId() != null) {
            jdbcTemplate.update("""
                    UPDATE property_checklists
                    SET source_user_checklist_id = ?, checklist_name_snapshot = ?
                    WHERE id = ?
                    """, checklist.getSourceUserChecklistId(), checklist.getName(), checklist.getId());
            return checklist;
        }
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO property_checklists (
                        property_id, source_user_checklist_id, checklist_name_snapshot, stage
                    ) VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, checklist.getPropertyId());
            statement.setLong(2, checklist.getSourceUserChecklistId());
            statement.setString(3, checklist.getName());
            statement.setString(4, checklist.getStage().name());
            return statement;
        }, keys);
        return AppliedChecklist.restore(
                keys.getKey().longValue(), checklist.getPropertyId(),
                checklist.getSourceUserChecklistId(), checklist.getName(), checklist.getStage());
    }

    @Override
    public void deleteItems(Long propertyChecklistId) {
        jdbcTemplate.update(
                "DELETE FROM property_checklist_items WHERE property_checklist_id = ?",
                propertyChecklistId);
    }

    @Override
    public List<AppliedChecklistItem> insertItems(
            Long propertyChecklistId,
            List<AppliedChecklistItemDraft> items) {
        List<AppliedChecklistItem> saved = new ArrayList<>();
        for (AppliedChecklistItemDraft item : items) {
            GeneratedKeyHolder keys = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement("""
                        INSERT INTO property_checklist_items (
                            property_checklist_id, source_system_check_item_id,
                            question_snapshot, guide_snapshot, display_order, status, memo
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, propertyChecklistId);
                statement.setLong(2, item.sourceSystemCheckItemId());
                statement.setString(3, item.question());
                statement.setString(4, item.guide());
                statement.setInt(5, item.displayOrder());
                statement.setString(6, item.status().name());
                statement.setString(7, item.memo());
                return statement;
            }, keys);
            saved.add(AppliedChecklistItem.restore(
                    keys.getKey().longValue(), propertyChecklistId,
                    item.sourceSystemCheckItemId(), item.question(), item.guide(),
                    item.displayOrder(), item.status(), item.memo()));
        }
        return saved;
    }

    @Override
    public int updateStatus(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            CheckStatus status) {
        return jdbcTemplate.update("""
                UPDATE property_checklist_items pci
                JOIN property_checklists pc ON pc.id = pci.property_checklist_id
                JOIN properties p ON p.id = pc.property_id
                SET pci.status = ?
                WHERE pci.id = ? AND pci.property_checklist_id = ?
                  AND pc.property_id = ? AND p.member_id = ?
                """, status.name(), itemId, propertyChecklistId, propertyId, memberId);
    }

    @Override
    public int updateMemo(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            String memo) {
        return jdbcTemplate.update("""
                UPDATE property_checklist_items pci
                JOIN property_checklists pc ON pc.id = pci.property_checklist_id
                JOIN properties p ON p.id = pc.property_id
                SET pci.memo = ?
                WHERE pci.id = ? AND pci.property_checklist_id = ?
                  AND pc.property_id = ? AND p.member_id = ?
                """, memo, itemId, propertyChecklistId, propertyId, memberId);
    }

    private AppliedChecklist mapChecklist(ResultSet resultSet, int rowNumber) throws SQLException {
        return AppliedChecklist.restore(
                resultSet.getLong("id"),
                resultSet.getLong("property_id"),
                resultSet.getObject("source_user_checklist_id", Long.class),
                resultSet.getString("checklist_name_snapshot"),
                Stage.valueOf(resultSet.getString("stage")));
    }

    private AppliedChecklistItem mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return AppliedChecklistItem.restore(
                resultSet.getLong("id"),
                resultSet.getLong("property_checklist_id"),
                resultSet.getLong("source_system_check_item_id"),
                resultSet.getString("question_snapshot"),
                resultSet.getString("guide_snapshot"),
                resultSet.getInt("display_order"),
                CheckStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("memo"));
    }

    private <T> Optional<T> first(List<T> values) {
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.getFirst());
    }
}
