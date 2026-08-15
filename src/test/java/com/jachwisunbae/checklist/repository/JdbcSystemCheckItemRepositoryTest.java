package com.jachwisunbae.checklist.repository;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

@Testcontainers
@SpringBootTest(properties = "spring.main.web-application-type=none")
class JdbcSystemCheckItemRepositoryTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    JdbcSystemCheckItemRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM system_check_items");
    }

    @Test
    void insertReturnsItemWithGeneratedId() {
        SystemCheckItem saved = repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러가 정상적으로 작동하는가?",
                "온수와 난방을 직접 확인합니다."));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getStage()).isEqualTo(Stage.ON_SITE);
                    assertThat(found.getItemType()).isEqualTo(ItemType.CORE);
                    assertThat(found.isActive()).isTrue();
                });
    }

    @Test
    void saveUpdatesExistingItem() {
        SystemCheckItem saved = repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "엘리베이터가 있는가?",
                null));
        saved.deactivate();

        repository.save(saved);

        assertThat(repository.findById(saved.getId()))
                .hasValueSatisfying(found -> assertThat(found.isActive()).isFalse());
    }

    @Test
    void findsActiveItemsByStage() {
        SystemCheckItem active = repository.save(SystemCheckItem.create(
                Stage.ONLINE_PHONE,
                ItemType.CORE,
                "관리비 포함 항목을 확인했는가?",
                null));
        SystemCheckItem inactive = repository.save(SystemCheckItem.create(
                Stage.ONLINE_PHONE,
                ItemType.OPTIONAL,
                "주차가 가능한가?",
                null));
        inactive.deactivate();
        repository.save(inactive);
        repository.save(SystemCheckItem.create(
                Stage.PRE_CONTRACT,
                ItemType.CORE,
                "등기부등본을 확인했는가?",
                null));

        assertThat(repository.findAllByStageAndActiveTrue(Stage.ONLINE_PHONE))
                .extracting(SystemCheckItem::getId)
                .containsExactly(active.getId());
    }

    @Test
    void findsActiveItemsWithFilters() {
        repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러가 정상적으로 작동하는가?",
                null));
        SystemCheckItem expected = repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "엘리베이터가 정상적으로 작동하는가?",
                null));
        repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "주차가 가능한가?",
                null));
        SystemCheckItem inactive = repository.save(SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "엘리베이터가 넓은가?",
                null));
        inactive.deactivate();
        repository.save(inactive);

        assertThat(repository.findActive(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "엘리베이터"))
                .extracting(SystemCheckItem::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void deletesItemById() {
        SystemCheckItem saved = repository.save(SystemCheckItem.create(
                Stage.PRE_CONTRACT,
                ItemType.OPTIONAL,
                "특약을 확인했는가?",
                null));

        repository.delete(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
