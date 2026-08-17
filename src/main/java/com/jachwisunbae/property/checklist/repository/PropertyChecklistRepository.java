package com.jachwisunbae.property.checklist.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.entity.PropertyChecklist;
import com.jachwisunbae.property.checklist.entity.PropertyChecklistItem;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public interface PropertyChecklistRepository {

    Optional<PropertyChecklist> findByPropertyAndStageForUpdate(Long propertyId, Stage stage);

    Optional<PropertyChecklist> findOwnedById(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId);

    List<PropertyChecklist> findAllOwned(Long memberId, Long propertyId);

    List<PropertyChecklistItem> findItems(Long propertyChecklistId);

    PropertyChecklist save(PropertyChecklist checklist);

    void deleteItems(Long propertyChecklistId);

    List<PropertyChecklistItem> insertItems(
            Long propertyChecklistId,
            List<NewPropertyChecklistItem> items);

    int updateStatus(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            CheckStatus status);

    int updateMemo(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            String memo);
}
