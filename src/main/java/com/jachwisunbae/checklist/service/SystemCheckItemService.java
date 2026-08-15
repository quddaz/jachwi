package com.jachwisunbae.checklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.service.dto.SystemCheckItemResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SystemCheckItemService {

    private final SystemCheckItemRepository systemCheckItemRepository;

    public SystemCheckItemService(SystemCheckItemRepository systemCheckItemRepository) {
        this.systemCheckItemRepository = systemCheckItemRepository;
    }

    public List<SystemCheckItemResult> search(
            Stage stage,
            ItemType itemType,
            String query) {
        String normalizedQuery = normalize(query);
        return systemCheckItemRepository
                .findActive(stage, itemType, normalizedQuery)
                .stream()
                .map(SystemCheckItemResult::from)
                .toList();
    }

    private String normalize(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }
}
