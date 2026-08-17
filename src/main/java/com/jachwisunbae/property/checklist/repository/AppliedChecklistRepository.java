package com.jachwisunbae.property.checklist.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.entity.AppliedChecklist;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItemDraft;
import com.jachwisunbae.property.checklist.type.CheckStatus;

public interface AppliedChecklistRepository {

    Optional<AppliedChecklist> findByPropertyAndStageForUpdate(Long propertyId, Stage stage);

    Optional<AppliedChecklist> findOwnedById(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId);

    List<AppliedChecklist> findAllOwned(Long memberId, Long propertyId);

    List<AppliedChecklistItem> findItems(Long propertyChecklistId);

    AppliedChecklist save(AppliedChecklist checklist);

    void deleteItems(Long propertyChecklistId);

    List<AppliedChecklistItem> insertItems(
            Long propertyChecklistId,
            List<AppliedChecklistItemDraft> items);

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
