package com.jachwisunbae.property.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.type.CheckStatus;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class PropertyChecklistServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    AppliedChecklistService service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;
    Long propertyId;
    Long commonItemId;
    Long firstOnlyItemId;
    Long secondOnlyItemId;
    Long firstChecklistId;
    Long secondChecklistId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM property_checklist_items");
        jdbcTemplate.update("DELETE FROM property_checklists");
        jdbcTemplate.update("DELETE FROM user_checklist_items");
        jdbcTemplate.update("DELETE FROM user_checklists");
        jdbcTemplate.update("DELETE FROM system_check_items");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM members");

        memberId = insertMember();
        propertyId = insertProperty();
        commonItemId = insertSystemItem("공통 질문", "공통 안내");
        firstOnlyItemId = insertSystemItem("첫 질문", null);
        secondOnlyItemId = insertSystemItem("둘째 질문", "둘째 안내");
        firstChecklistId = insertChecklist("첫 체크", List.of(commonItemId, firstOnlyItemId));
        secondChecklistId = insertChecklist("둘째 체크", List.of(commonItemId, secondOnlyItemId));
    }

    @Test
    void appliesSnapshotsAndReplacementInheritsResultsBySystemItemId() {
        var applied = service.applyOrReplace(
                memberId, propertyId, Stage.ON_SITE, firstChecklistId);
        Long oldCommonItemId = applied.items().getFirst().itemId();
        service.updateMemo(
                memberId, propertyId, applied.propertyChecklistId(), oldCommonItemId, "좋았음");
        service.updateStatus(
                memberId, propertyId, applied.propertyChecklistId(), oldCommonItemId,
                CheckStatus.GOOD);

        var replaced = service.applyOrReplace(
                memberId, propertyId, Stage.ON_SITE, secondChecklistId);

        assertThat(replaced.name()).isEqualTo("둘째 체크");
        assertThat(replaced.items()).extracting(item -> item.question())
                .containsExactly("공통 질문", "둘째 질문");
        assertThat(replaced.items().getFirst().itemId()).isNotEqualTo(oldCommonItemId);
        assertThat(replaced.items().getFirst().status()).isEqualTo(CheckStatus.GOOD);
        assertThat(replaced.items().getFirst().memo()).isEqualTo("좋았음");
        assertThat(replaced.items().get(1).status()).isEqualTo(CheckStatus.UNCONFIRMED);
    }

    @Test
    void statusAndMemoUpdatesAreIndependentAndProgressIsAggregated() {
        var applied = service.applyOrReplace(
                memberId, propertyId, Stage.ON_SITE, firstChecklistId);
        Long itemId = applied.items().getFirst().itemId();

        service.updateMemo(
                memberId, propertyId, applied.propertyChecklistId(), itemId, "메모 유지");
        service.updateStatus(
                memberId, propertyId, applied.propertyChecklistId(), itemId, CheckStatus.CAUTION);
        var detail = service.findOne(memberId, propertyId, applied.propertyChecklistId());

        assertThat(detail.items().getFirst().memo()).isEqualTo("메모 유지");
        assertThat(detail.items().getFirst().status()).isEqualTo(CheckStatus.CAUTION);
        assertThat(detail.progress().totalCount()).isEqualTo(2);
        assertThat(detail.progress().completedCount()).isOne();
        assertThat(detail.progress().progressPercent()).isEqualTo(50);
    }

    @Test
    void listsApplicationStateForEveryStage() {
        service.applyOrReplace(memberId, propertyId, Stage.ON_SITE, firstChecklistId);

        var summaries = service.findAll(memberId, propertyId);

        assertThat(summaries).extracting(summary -> summary.stage())
                .containsExactly(Stage.ONLINE_PHONE, Stage.ON_SITE, Stage.PRE_CONTRACT);
        assertThat(summaries).extracting(summary -> summary.applied())
                .containsExactly(false, true, false);
    }

    private Long insertMember() {
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES ('check-owner', 'check@example.com', '회원', CURRENT_TIMESTAMP(6))
                """);
        return jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
    }

    private Long insertProperty() {
        jdbcTemplate.update("""
                INSERT INTO properties (member_id, name, last_activity_at)
                VALUES (?, '체크 매물', CURRENT_TIMESTAMP(6))
                """, memberId);
        return jdbcTemplate.queryForObject("SELECT id FROM properties", Long.class);
    }

    private Long insertSystemItem(String question, String guide) {
        jdbcTemplate.update("""
                INSERT INTO system_check_items (stage, item_type, question, guide)
                VALUES ('ON_SITE', 'OPTIONAL', ?, ?)
                """, question, guide);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM system_check_items WHERE question = ?", Long.class, question);
    }

    private Long insertChecklist(String name, List<Long> itemIds) {
        jdbcTemplate.update("""
                INSERT INTO user_checklists (member_id, name, stage)
                VALUES (?, ?, 'ON_SITE')
                """, memberId, name);
        Long checklistId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_checklists WHERE name = ?", Long.class, name);
        for (int index = 0; index < itemIds.size(); index++) {
            jdbcTemplate.update("""
                    INSERT INTO user_checklist_items (
                        user_checklist_id, system_check_item_id, display_order
                    ) VALUES (?, ?, ?)
                    """, checklistId, itemIds.get(index), index + 1);
        }
        return checklistId;
    }
}
