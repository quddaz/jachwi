package com.jachwisunbae.property.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

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

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.service.dto.CreatePropertyCommand;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class PropertyServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    PropertyService service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;
    Long otherMemberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM members");
        memberId = insertMember("property-owner");
        otherMemberId = insertMember("other-owner");
    }

    @Test
    void createsPropertyWhilePreservingNullAndZeroAmounts() {
        var result = service.create(memberId, new CreatePropertyCommand(
                "  신림 원룸  ", null, 0L, 80_000L, "서울 관악구", null));

        assertThat(result.name()).isEqualTo("신림 원룸");
        assertThat(result.depositAmount()).isNull();
        assertThat(result.monthlyRentAmount()).isZero();
    }

    @Test
    void searchesSortsAndPagesOwnedProperties() {
        insertProperty(memberId, "신림 오래된 집", LocalDateTime.of(2026, 8, 16, 0, 0));
        insertProperty(memberId, "신림 새 집", LocalDateTime.of(2026, 8, 17, 0, 0));
        insertProperty(otherMemberId, "신림 타인 집", LocalDateTime.of(2026, 8, 18, 0, 0));

        var page = service.findAll(memberId, " 신림 ", 0, 1);

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(item -> item.name()).containsExactly("신림 새 집");
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void hidesOtherMembersPropertyAsNotFound() {
        Long propertyId = insertProperty(
                otherMemberId,
                "타인 매물",
                LocalDateTime.of(2026, 8, 17, 0, 0));

        assertThatThrownBy(() -> service.findOne(memberId, propertyId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(DomainErrorCode.PROPERTY_NOT_FOUND));
    }

    @Test
    void rejectsFiftyFirstProperty() {
        for (int index = 1; index <= 50; index++) {
            insertProperty(memberId, "매물 " + index, LocalDateTime.of(2026, 8, 17, 0, 0));
        }

        assertThatThrownBy(() -> service.create(memberId, new CreatePropertyCommand(
                "51번째", null, null, null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(DomainErrorCode.PROPERTY_LIMIT_EXCEEDED));
    }

    private Long insertMember(String subject) {
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES (?, ?, '회원', CURRENT_TIMESTAMP(6))
                """, subject, subject + "@example.com");
        return jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE subject = ?", Long.class, subject);
    }

    private Long insertProperty(Long ownerId, String name, LocalDateTime activityAt) {
        jdbcTemplate.update("""
                INSERT INTO properties (member_id, name, last_activity_at)
                VALUES (?, ?, ?)
                """, ownerId, name, activityAt);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM properties WHERE member_id = ? AND name = ?",
                Long.class,
                ownerId,
                name);
    }
}
