package com.jachwisunbae.property.repository;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.jachwisunbae.property.entity.Property;

@Repository
public class JdbcPropertyRepository implements PropertyRepository {

    private static final RowMapper<PropertyRow> ROW_MAPPER = (resultSet, rowNumber) ->
            new PropertyRow(
                    Property.restore(
                            resultSet.getLong("id"),
                            resultSet.getLong("member_id"),
                            resultSet.getString("name"),
                            resultSet.getObject("deposit_amount", Long.class),
                            resultSet.getObject("monthly_rent_amount", Long.class),
                            resultSet.getObject("maintenance_fee_amount", Long.class),
                            resultSet.getString("address"),
                            resultSet.getString("discovery_source"),
                            resultSet.getTimestamp("last_activity_at").toLocalDateTime(),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getTimestamp("updated_at").toLocalDateTime()),
                    resultSet.getLong("total_count"),
                    resultSet.getLong("completed_count"),
                    resultSet.getLong("good_count"),
                    resultSet.getLong("caution_count"),
                    resultSet.getLong("unconfirmed_count"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcPropertyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean lockMember(Long memberId) {
        return !jdbcTemplate.queryForList(
                "SELECT id FROM members WHERE id = ? FOR UPDATE", Long.class, memberId).isEmpty();
    }

    @Override
    public long countByMemberId(Long memberId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM properties WHERE member_id = ?", Long.class, memberId);
    }

    @Override
    public long countByMemberIdAndQuery(Long memberId, String query) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM properties
                WHERE member_id = ? AND LOCATE(?, name) > 0
                """, Long.class, memberId, query);
    }

    @Override
    public Property save(Property property) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO properties (
                        member_id, name, deposit_amount, monthly_rent_amount,
                        maintenance_fee_amount, address, discovery_source, last_activity_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, property.getMemberId());
            statement.setString(2, property.getName());
            statement.setObject(3, property.getDepositAmount());
            statement.setObject(4, property.getMonthlyRentAmount());
            statement.setObject(5, property.getMaintenanceFeeAmount());
            statement.setString(6, property.getAddress());
            statement.setString(7, property.getDiscoverySource());
            statement.setObject(8, property.getLastActivityAt());
            return statement;
        }, keys);
        return Property.restore(
                keys.getKey().longValue(), property.getMemberId(), property.getName(),
                property.getDepositAmount(), property.getMonthlyRentAmount(),
                property.getMaintenanceFeeAmount(), property.getAddress(),
                property.getDiscoverySource(), property.getLastActivityAt(), null, null);
    }

    @Override
    public List<PropertyRow> findPageByMemberId(
            Long memberId,
            String query,
            int size,
            long offset) {
        return jdbcTemplate.query("""
                SELECT p.id, p.member_id, p.name, p.deposit_amount, p.monthly_rent_amount,
                       p.maintenance_fee_amount, p.address, p.discovery_source,
                       p.last_activity_at, p.created_at, p.updated_at,
                       COUNT(pci.id) AS total_count,
                       COALESCE(SUM(CASE WHEN pci.status IN ('GOOD', 'CAUTION') THEN 1 ELSE 0 END), 0)
                           AS completed_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'GOOD' THEN 1 ELSE 0 END), 0)
                           AS good_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'CAUTION' THEN 1 ELSE 0 END), 0)
                           AS caution_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'UNCONFIRMED' THEN 1 ELSE 0 END), 0)
                           AS unconfirmed_count
                FROM properties p
                LEFT JOIN property_checklists pc ON pc.property_id = p.id
                LEFT JOIN property_checklist_items pci ON pci.property_checklist_id = pc.id
                WHERE p.member_id = ? AND LOCATE(?, p.name) > 0
                GROUP BY p.id
                ORDER BY p.last_activity_at DESC, p.id DESC
                LIMIT ? OFFSET ?
                """, ROW_MAPPER, memberId, query, size, offset);
    }

    @Override
    public Optional<PropertyRow> findByIdAndMemberId(Long propertyId, Long memberId) {
        return jdbcTemplate.query("""
                SELECT p.id, p.member_id, p.name, p.deposit_amount, p.monthly_rent_amount,
                       p.maintenance_fee_amount, p.address, p.discovery_source,
                       p.last_activity_at, p.created_at, p.updated_at,
                       COUNT(pci.id) AS total_count,
                       COALESCE(SUM(CASE WHEN pci.status IN ('GOOD', 'CAUTION') THEN 1 ELSE 0 END), 0)
                           AS completed_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'GOOD' THEN 1 ELSE 0 END), 0)
                           AS good_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'CAUTION' THEN 1 ELSE 0 END), 0)
                           AS caution_count,
                       COALESCE(SUM(CASE WHEN pci.status = 'UNCONFIRMED' THEN 1 ELSE 0 END), 0)
                           AS unconfirmed_count
                FROM properties p
                LEFT JOIN property_checklists pc ON pc.property_id = p.id
                LEFT JOIN property_checklist_items pci ON pci.property_checklist_id = pc.id
                WHERE p.id = ? AND p.member_id = ?
                GROUP BY p.id
                """, ROW_MAPPER, propertyId, memberId).stream().findFirst();
    }

    @Override
    public int update(Property property) {
        return jdbcTemplate.update("""
                UPDATE properties
                SET name = ?, deposit_amount = ?, monthly_rent_amount = ?,
                    maintenance_fee_amount = ?, address = ?, discovery_source = ?,
                    last_activity_at = ?
                WHERE id = ? AND member_id = ?
                """, property.getName(), property.getDepositAmount(),
                property.getMonthlyRentAmount(), property.getMaintenanceFeeAmount(),
                property.getAddress(), property.getDiscoverySource(), property.getLastActivityAt(),
                property.getId(), property.getMemberId());
    }

    @Override
    public int deleteByIdAndMemberId(Long propertyId, Long memberId) {
        return jdbcTemplate.update(
                "DELETE FROM properties WHERE id = ? AND member_id = ?", propertyId, memberId);
    }

    @Override
    public int touch(Long propertyId, Long memberId, LocalDateTime activityAt) {
        return jdbcTemplate.update("""
                UPDATE properties SET last_activity_at = ?
                WHERE id = ? AND member_id = ?
                """, activityAt, propertyId, memberId);
    }
}
