package com.jachwisunbae.property.checklist.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.jachwisunbae.property.checklist.entity.PropertyChecklist;
import com.jachwisunbae.property.checklist.entity.PropertyChecklistItem;
import com.jachwisunbae.property.checklist.repository.NewPropertyChecklistItem;
import com.jachwisunbae.property.checklist.repository.PropertyChecklistRepository;
import com.jachwisunbae.property.checklist.service.dto.CheckProgressResult;
import com.jachwisunbae.property.checklist.service.dto.PropertyChecklistDetailResult;
import com.jachwisunbae.property.checklist.service.dto.PropertyChecklistSummaryResult;
import com.jachwisunbae.property.checklist.service.validation.PropertyChecklistValidator;
import com.jachwisunbae.property.checklist.type.CheckStatus;
import com.jachwisunbae.property.repository.PropertyRepository;

@Service
public class PropertyChecklistService {

    private final PropertyChecklistRepository repository;
    private final PropertyRepository propertyRepository;
    private final UserChecklistRepository userChecklistRepository;
    private final SystemCheckItemRepository systemCheckItemRepository;
    private final PropertyChecklistValidator validator;
    private final Clock clock;

    public PropertyChecklistService(
            PropertyChecklistRepository repository,
            PropertyRepository propertyRepository,
            UserChecklistRepository userChecklistRepository,
            SystemCheckItemRepository systemCheckItemRepository,
            PropertyChecklistValidator validator,
            Clock clock) {
        this.repository = repository;
        this.propertyRepository = propertyRepository;
        this.userChecklistRepository = userChecklistRepository;
        this.systemCheckItemRepository = systemCheckItemRepository;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public PropertyChecklistDetailResult applyOrReplace(
            Long memberId,
            Long propertyId,
            Stage stage,
            Long sourceChecklistId) {
        validatePropertyOwnership(memberId, propertyId);
        UserChecklist source = findSourceChecklist(memberId, sourceChecklistId);
        validator.validateStage(stage, source.getStage());

        List<UserChecklistItem> sourceItems = userChecklistRepository.findItems(sourceChecklistId);
        Map<Long, SystemCheckItem> systemItems = findSystemItems(sourceItems);
        PropertyChecklist checklist = saveRoot(propertyId, stage, source);
        Map<Long, PreviousResult> previousResults = findPreviousResults(checklist.getId());

        repository.deleteItems(checklist.getId());
        repository.insertItems(
                checklist.getId(),
                createSnapshotItems(sourceItems, systemItems, previousResults));
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
        return findOne(memberId, propertyId, checklist.getId());
    }

    @Transactional(readOnly = true)
    public List<PropertyChecklistSummaryResult> findAll(Long memberId, Long propertyId) {
        validatePropertyOwnership(memberId, propertyId);
        Map<Stage, PropertyChecklist> byStage = repository.findAllOwned(memberId, propertyId)
                .stream().collect(Collectors.toMap(PropertyChecklist::getStage, Function.identity()));
        return List.of(Stage.values()).stream()
                .map(stage -> toSummary(stage, byStage.get(stage)))
                .toList();
    }

    private PropertyChecklistSummaryResult toSummary(
            Stage stage,
            PropertyChecklist checklist) {
        if (checklist == null) {
            return new PropertyChecklistSummaryResult(
                    null, null, stage, false, CheckProgressResult.from(List.of()));
        }
        return new PropertyChecklistSummaryResult(
                checklist.getId(), checklist.getName(), stage, true,
                CheckProgressResult.from(repository.findItems(checklist.getId())));
    }

    @Transactional(readOnly = true)
    public PropertyChecklistDetailResult findOne(
            Long memberId,
            Long propertyId,
            Long propertyChecklistId) {
        PropertyChecklist checklist = repository.findOwnedById(
                memberId, propertyId, propertyChecklistId).orElseThrow(this::notFound);
        return PropertyChecklistDetailResult.from(
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
        return userChecklistRepository.findActiveByIdAndMemberIdForUpdate(
                sourceChecklistId, memberId).orElseThrow(() -> new BusinessException(
                        DomainErrorCode.CHECKLIST_NOT_FOUND,
                        "적용할 사용자 체크리스트를 찾을 수 없습니다."));
    }

    private Map<Long, SystemCheckItem> findSystemItems(List<UserChecklistItem> sourceItems) {
        List<Long> ids = sourceItems.stream().map(UserChecklistItem::getSystemCheckItemId).toList();
        return systemCheckItemRepository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(SystemCheckItem::getId, Function.identity()));
    }

    private PropertyChecklist saveRoot(
            Long propertyId,
            Stage stage,
            UserChecklist source) {
        return repository.findByPropertyAndStageForUpdate(propertyId, stage)
                .map(existing -> repository.save(PropertyChecklist.restore(
                        existing.getId(), propertyId, source.getId(), source.getName(), stage)))
                .orElseGet(() -> repository.save(PropertyChecklist.create(
                        propertyId, source.getId(), source.getName(), stage)));
    }

    private Map<Long, PreviousResult> findPreviousResults(Long propertyChecklistId) {
        Map<Long, PreviousResult> previous = new HashMap<>();
        for (PropertyChecklistItem item : repository.findItems(propertyChecklistId)) {
            previous.put(
                    item.getSourceSystemCheckItemId(),
                    new PreviousResult(item.getStatus(), item.getMemo()));
        }
        return previous;
    }

    private List<NewPropertyChecklistItem> createSnapshotItems(
            List<UserChecklistItem> sourceItems,
            Map<Long, SystemCheckItem> systemItems,
            Map<Long, PreviousResult> previousResults) {
        return sourceItems.stream().map(item -> {
            SystemCheckItem systemItem = systemItems.get(item.getSystemCheckItemId());
            if (systemItem == null) {
                throw new BusinessException(
                        DomainErrorCode.CHECKLIST_ITEM_NOT_FOUND,
                        "시스템 체크 항목을 찾을 수 없습니다.");
            }
            PreviousResult previous = previousResults.get(item.getSystemCheckItemId());
            return new NewPropertyChecklistItem(
                    systemItem.getId(), systemItem.getQuestion(), systemItem.getGuide(),
                    item.getDisplayOrder(),
                    previous == null ? CheckStatus.UNCONFIRMED : previous.status(),
                    previous == null ? "" : previous.memo());
        }).toList();
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

    private record PreviousResult(CheckStatus status, String memo) {
    }
}
