package com.jachwisunbae.property.checklist.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.repository.UserChecklistRepository;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.checklist.entity.AppliedChecklist;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItem;
import com.jachwisunbae.property.checklist.entity.AppliedChecklistItemDraft;
import com.jachwisunbae.property.checklist.repository.AppliedChecklistRepository;
import com.jachwisunbae.property.checklist.service.dto.CheckProgressResult;
import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistDetailResult;
import com.jachwisunbae.property.checklist.service.dto.AppliedChecklistSummaryResult;
import com.jachwisunbae.property.checklist.service.validation.AppliedChecklistValidator;
import com.jachwisunbae.property.checklist.type.CheckStatus;
import com.jachwisunbae.property.repository.PropertyRepository;

@Service
public class AppliedChecklistService {

    private final AppliedChecklistRepository repository;
    private final PropertyRepository propertyRepository;
    private final UserChecklistRepository userChecklistRepository;
    private final SystemCheckItemRepository systemCheckItemRepository;
    private final AppliedChecklistValidator validator;
    private final Clock clock;

    public AppliedChecklistService(
            AppliedChecklistRepository repository,
            PropertyRepository propertyRepository,
            UserChecklistRepository userChecklistRepository,
            SystemCheckItemRepository systemCheckItemRepository,
            AppliedChecklistValidator validator,
            Clock clock) {
        this.repository = repository;
        this.propertyRepository = propertyRepository;
        this.userChecklistRepository = userChecklistRepository;
        this.systemCheckItemRepository = systemCheckItemRepository;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public AppliedChecklistDetailResult applyOrReplace(
            Long memberId,
            Long propertyId,
            Stage stage,
            Long sourceChecklistId) {
        validatePropertyOwnership(memberId, propertyId);
        UserChecklist source = findSourceChecklist(memberId, sourceChecklistId);
        validator.validateStage(stage, source.getStage());

        List<UserChecklistItem> sourceItems = userChecklistRepository.findItems(sourceChecklistId);
        Map<Long, SystemCheckItem> systemItems = findSystemItems(sourceItems);
        AppliedChecklist checklist = saveRoot(propertyId, stage, source);
        Map<Long, PreviousCheckResult> previousResults = findPreviousResults(checklist.getId());

        repository.deleteItems(checklist.getId());
        repository.insertItems(
                checklist.getId(),
                createSnapshotItems(sourceItems, systemItems, previousResults));
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
        return findOne(memberId, propertyId, checklist.getId());
    }

    @Transactional(readOnly = true)
    public List<AppliedChecklistSummaryResult> findAll(Long memberId, Long propertyId) {
        validatePropertyOwnership(memberId, propertyId);
        Map<Stage, AppliedChecklist> byStage = mapChecklistsByStage(
                repository.findAllOwned(memberId, propertyId));
        List<AppliedChecklistSummaryResult> summaries = new ArrayList<>();
        for (Stage stage : Stage.values()) {
            summaries.add(toSummary(stage, byStage.get(stage)));
        }
        return List.copyOf(summaries);
    }

    private AppliedChecklistSummaryResult toSummary(
            Stage stage,
            AppliedChecklist checklist) {
        if (checklist == null) {
            return new AppliedChecklistSummaryResult(
                    null, null, stage, false, CheckProgressResult.from(List.of()));
        }
        return new AppliedChecklistSummaryResult(
                checklist.getId(), checklist.getName(), stage, true,
                CheckProgressResult.from(repository.findItems(checklist.getId())));
    }

    private Map<Stage, AppliedChecklist> mapChecklistsByStage(
            List<AppliedChecklist> checklists) {
        Map<Stage, AppliedChecklist> byStage = new EnumMap<>(Stage.class);
        for (AppliedChecklist checklist : checklists) {
            byStage.put(checklist.getStage(), checklist);
        }
        return byStage;
    }

    @Transactional(readOnly = true)
    public AppliedChecklistDetailResult findOne(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId) {
        AppliedChecklist checklist = repository.findOwnedById(
                memberId, propertyId, propertyChecklistId).orElseThrow(this::notFound);
        return AppliedChecklistDetailResult.from(
                checklist, repository.findItems(propertyChecklistId));
    }

    @Transactional
    public void updateStatus(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            CheckStatus status) {
        validator.validateStatus(status);
        if (repository.updateStatus(
                memberId, propertyId, propertyChecklistId, itemId, status) == 0) {
            throw itemNotFound();
        }
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
    }

    @Transactional
    public void updateMemo(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId,
            Long itemId,
            String memo) {
        String validatedMemo = validator.validateMemo(memo);
        if (repository.updateMemo(
                memberId, propertyId, propertyChecklistId, itemId, validatedMemo) == 0) {
            throw itemNotFound();
        }
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
    }

    private void validatePropertyOwnership(Long memberId, Long propertyId) {
        if (propertyRepository.findByIdAndMemberId(propertyId, memberId).isEmpty()) {
            throw new BusinessException(DomainErrorCode.PROPERTY_NOT_FOUND, "매물을 찾을 수 없습니다.");
        }
    }

    private UserChecklist findSourceChecklist(Long memberId, Long sourceChecklistId) {
        Optional<UserChecklist> source = userChecklistRepository
                .findActiveByIdAndMemberIdForUpdate(sourceChecklistId, memberId);
        if (source.isEmpty()) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_NOT_FOUND,
                    "적용할 사용자 체크리스트를 찾을 수 없습니다.");
        }
        return source.get();
    }

    private Map<Long, SystemCheckItem> findSystemItems(List<UserChecklistItem> sourceItems) {
        List<Long> ids = new ArrayList<>();
        for (UserChecklistItem sourceItem : sourceItems) {
            ids.add(sourceItem.getSystemCheckItemId());
        }
        Map<Long, SystemCheckItem> systemItems = new HashMap<>();
        for (SystemCheckItem systemItem : systemCheckItemRepository.findAllByIds(ids)) {
            systemItems.put(systemItem.getId(), systemItem);
        }
        return systemItems;
    }

    private AppliedChecklist saveRoot(
            Long propertyId,
            Stage stage,
            UserChecklist source) {
        Optional<AppliedChecklist> existing = repository.findByPropertyAndStageForUpdate(
                propertyId, stage);
        if (existing.isPresent()) {
            AppliedChecklist replacement = AppliedChecklist.restore(
                    existing.get().getId(), propertyId, source.getId(), source.getName(), stage);
            return repository.save(replacement);
        }
        return repository.save(AppliedChecklist.create(
                propertyId, source.getId(), source.getName(), stage));
    }

    private Map<Long, PreviousCheckResult> findPreviousResults(Long propertyChecklistId) {
        Map<Long, PreviousCheckResult> previous = new HashMap<>();
        for (AppliedChecklistItem item : repository.findItems(propertyChecklistId)) {
            previous.put(
                    item.getSourceSystemCheckItemId(),
                    new PreviousCheckResult(item.getStatus(), item.getMemo()));
        }
        return previous;
    }

    private List<AppliedChecklistItemDraft> createSnapshotItems(
            List<UserChecklistItem> sourceItems,
            Map<Long, SystemCheckItem> systemItems,
            Map<Long, PreviousCheckResult> previousResults) {
        List<AppliedChecklistItemDraft> snapshots = new ArrayList<>();
        for (UserChecklistItem item : sourceItems) {
            SystemCheckItem systemItem = systemItems.get(item.getSystemCheckItemId());
            if (systemItem == null) {
                throw new BusinessException(
                        DomainErrorCode.CHECKLIST_ITEM_NOT_FOUND,
                        "시스템 체크 항목을 찾을 수 없습니다.");
            }
            PreviousCheckResult previous = previousResults.get(item.getSystemCheckItemId());
            snapshots.add(new AppliedChecklistItemDraft(
                    systemItem.getId(), systemItem.getQuestion(), systemItem.getGuide(),
                    item.getDisplayOrder(),
                    previous == null ? CheckStatus.UNCONFIRMED : previous.status(),
                    previous == null ? "" : previous.memo()));
        }
        return List.copyOf(snapshots);
    }

    private BusinessException notFound() {
        return new BusinessException(
                DomainErrorCode.PROPERTY_CHECKLIST_NOT_FOUND,
                "매물 체크리스트를 찾을 수 없습니다.");
    }

    private BusinessException itemNotFound() {
        return new BusinessException(
                DomainErrorCode.PROPERTY_CHECKLIST_ITEM_NOT_FOUND,
                "매물 체크 항목을 찾을 수 없습니다.");
    }
}
