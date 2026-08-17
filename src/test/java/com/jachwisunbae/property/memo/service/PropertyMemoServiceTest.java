package com.jachwisunbae.property.memo.service;

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

import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand.Item;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class PropertyMemoServiceTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    PropertyMemoService service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long memberId;
    Long propertyId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM property_memo_items");
        jdbcTemplate.update("DELETE FROM property_memos");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM members");
        jdbcTemplate.update("""
                INSERT INTO members (subject, email, name, last_login_at)
                VALUES ('memo-owner', 'memo@example.com', '회원', CURRENT_TIMESTAMP(6))
                """);
        memberId = jdbcTemplate.queryForObject("SELECT id FROM members", Long.class);
        jdbcTemplate.update("""
                INSERT INTO properties (member_id, name, last_activity_at)
                VALUES (?, '메모 매물', CURRENT_TIMESTAMP(6))
                """, memberId);
        propertyId = jdbcTemplate.queryForObject("SELECT id FROM properties", Long.class);
    }

    @Test
    void returnsEmptyMemoWhenMemoRootDoesNotExist() {
        var result = service.get(memberId, propertyId);

        assertThat(result.items()).isEmpty();
        assertThat(result.freeMemo()).isEmpty();
        assertThat(result.savedAt()).isNull();
    }

    @Test
    void replacesFreeMemoAndStructuredItemsInRequestOrder() {
        service.replace(memberId, propertyId, new ReplacePropertyMemoCommand(
                List.of(new Item("입주일", "9월 1일"), new Item("관리비", "수도 포함")),
                "채광 재확인"));

        var result = service.get(memberId, propertyId);

        assertThat(result.freeMemo()).isEqualTo("채광 재확인");
        assertThat(result.items()).extracting(item -> item.label())
                .containsExactly("입주일", "관리비");
        assertThat(result.items()).extracting(item -> item.order()).containsExactly(1, 2);
    }
}
