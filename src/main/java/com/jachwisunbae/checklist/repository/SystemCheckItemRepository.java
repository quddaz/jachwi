package com.jachwisunbae.checklist.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

public interface SystemCheckItemRepository {

    SystemCheckItem save(SystemCheckItem item);

    Optional<SystemCheckItem> findById(Long id);

    List<SystemCheckItem> findActive(Stage stage, ItemType itemType, String query);

    List<SystemCheckItem> findActiveCoreByStage(Stage stage);

    List<SystemCheckItem> findAllByIds(List<Long> ids);

    void delete(Long id);
}
