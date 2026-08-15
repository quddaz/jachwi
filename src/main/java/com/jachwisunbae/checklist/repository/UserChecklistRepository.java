package com.jachwisunbae.checklist.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.type.Stage;

public interface UserChecklistRepository {

    UserChecklist save(UserChecklist checklist);

    Optional<UserChecklist> findActiveByIdAndMemberId(Long checklistId, Long memberId);

    Optional<UserChecklist> findActiveByIdAndMemberIdForUpdate(Long checklistId, Long memberId);

    List<UserChecklist> findAllActiveByMemberId(Long memberId, Stage stage);

    List<UserChecklistItem> findItems(Long checklistId);

    void replaceItems(Long checklistId, List<Long> systemCheckItemIds);

    int countAppliedProperties(Long checklistId);

    void softDelete(Long checklistId);
}
