package com.jachwisunbae;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class FlywayMigrationTests {

	@Container
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

	@DynamicPropertySource
	static void mysqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void flywayCreatesTheCompleteMvpSchema() {
		List<String> tableNames = jdbcTemplate.queryForList("""
				SELECT table_name
				FROM information_schema.tables
				WHERE table_schema = DATABASE()
				""", String.class);

		assertThat(tableNames).contains(
				"flyway_schema_history",
				"members",
				"refresh_tokens",
				"properties",
				"property_photos",
				"property_memos",
				"property_memo_items",
				"system_check_items",
				"user_checklists",
				"user_checklist_items",
				"property_checklists",
				"property_checklist_items");
	}

	@Test
	void propertiesFollowTheApprovedModelWithoutDetailAddress() {
		List<String> columnNames = jdbcTemplate.queryForList("""
				SELECT column_name
				FROM information_schema.columns
				WHERE table_schema = DATABASE() AND table_name = 'properties'
				""", String.class);

		assertThat(columnNames)
				.contains("member_id", "name", "deposit_amount", "monthly_rent_amount",
						"maintenance_fee_amount", "address", "discovery_source", "last_activity_at")
				.doesNotContain("detail_address");
	}

	@Test
	void schemaEnforcesSnapshotAndOrderingUniqueness() {
		List<String> uniqueConstraints = jdbcTemplate.queryForList("""
				SELECT DISTINCT constraint_name
				FROM information_schema.table_constraints
				WHERE table_schema = DATABASE()
				  AND constraint_type = 'UNIQUE'
				""", String.class);

		assertThat(uniqueConstraints).contains(
				"uk_members_subject",
				"uk_refresh_tokens_token_hash",
				"uk_property_memos_property",
				"uk_property_memo_items_order",
				"uk_user_checklist_items_system_item",
				"uk_user_checklist_items_order",
				"uk_property_checklists_stage",
				"uk_property_checklist_items_system_item",
				"uk_property_checklist_items_order");
	}

	@Test
	void schemaProvidesIndexesForOwnedAndOrderedQueries() {
		List<String> indexNames = jdbcTemplate.queryForList("""
				SELECT DISTINCT index_name
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				""", String.class);

		assertThat(indexNames).contains(
				"idx_properties_member_activity",
				"idx_property_photos_property_created",
				"idx_system_check_items_search",
				"idx_user_checklists_member_active",
				"idx_property_checklists_property");
	}

	@Test
	void auditTimestampsAreManagedByMySqlForEveryTable() {
		List<String> timestampColumns = jdbcTemplate.queryForList("""
				SELECT CONCAT(column_name, ':', COALESCE(column_default, 'NULL'), ':', extra)
				FROM information_schema.columns
				WHERE table_schema = DATABASE()
				  AND table_name IN (
				      'members', 'refresh_tokens', 'properties', 'property_photos',
				      'property_memos', 'property_memo_items', 'system_check_items',
				      'user_checklists', 'user_checklist_items', 'property_checklists',
				      'property_checklist_items'
				  )
				  AND column_name IN ('created_at', 'updated_at')
				ORDER BY table_name, column_name
				""", String.class);

		assertThat(timestampColumns).hasSize(19);
		assertThat(timestampColumns)
				.filteredOn(value -> value.startsWith("created_at:"))
				.allMatch("created_at:CURRENT_TIMESTAMP(6):DEFAULT_GENERATED"::equals);
		assertThat(timestampColumns)
				.filteredOn(value -> value.startsWith("updated_at:"))
				.allMatch("updated_at:CURRENT_TIMESTAMP(6):DEFAULT_GENERATED on update CURRENT_TIMESTAMP(6)"::equals);
	}
}
