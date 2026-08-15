package com.jachwisunbae.checklist.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.repository.UserChecklistRepository;
import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UpdateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.service.dto.UserChecklistSummaryResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class UserChecklistService {

    private static final int MAX_ITEM_COUNT = 100;

    private final UserChecklistRepository checklistRepository;
    private final SystemCheckItemRepository systemItemRepository;
    private final MemberRepository memberRepository;

    public UserChecklistService(
            UserChecklistRepository checklistRepository,
            SystemCheckItemRepository systemItemRepository,
            MemberRepository memberRepository) {
        this.checklistRepository = checklistRepository;
        this.systemItemRepository = systemItemRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public UserChecklistDetailResult create(Long memberId, CreateUserChecklistCommand command) {
        validateMember(memberId);
        List<Long> itemIds = resolveCreateItemIds(command);
        UserChecklist checklist = saveChecklist(memberId, command, itemIds);
        return createDetailResult(checklist);
    }

    public List<UserChecklistSummaryResult> findAll(Long memberId, Stage stage) {
        return checklistRepository.findAllActiveByMemberId(memberId, stage).stream()
                .map(checklist -> UserChecklistSummaryResult.from(
                        checklist,
                        checklistRepository.findItems(checklist.getId()).size(),
                        checklistRepository.countAppliedProperties(checklist.getId())))
                .toList();
    }

    public UserChecklistDetailResult findById(Long memberId, Long checklistId) {
        return createDetailResult(getOwnedChecklist(memberId, checklistId));
    }

    @Transactional
    public UserChecklistDetailResult update(
            Long memberId,
            Long checklistId,
            UpdateUserChecklistCommand command) {
        UserChecklist checklist = getOwnedChecklistForUpdate(memberId, checklistId);
        List<Long> itemIds = validateUpdateItems(checklist, command.checkItemIds());
        checklist.rename(command.name());
        checklistRepository.save(checklist);
        checklistRepository.replaceItems(checklistId, itemIds);
        return createDetailResult(checklist);
    }

    @Transactional
    public void delete(Long memberId, Long checklistId) {
        getOwnedChecklistForUpdate(memberId, checklistId);
        checklistRepository.softDelete(checklistId);
    }

    private void validateMember(Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(
                DomainErrorCode.MEMBER_NOT_FOUND,
                "Access Token의 회원을 찾을 수 없습니다."));
    }

    private List<Long> resolveCreateItemIds(CreateUserChecklistCommand command) {
        List<Long> requestedIds = requireDistinctIds(command.checkItemIds(), true);
        validateStageAndActive(command.stage(), findRequestedItems(requestedIds), Set.of());

        List<Long> finalIds = new ArrayList<>();
        systemItemRepository.findActiveCoreByStage(command.stage()).stream()
                .map(SystemCheckItem::getId)
                .forEach(finalIds::add);
        requestedIds.stream().filter(id -> !finalIds.contains(id)).forEach(finalIds::add);
        requireItemCount(finalIds);
        return finalIds;
    }

    private UserChecklist saveChecklist(
            Long memberId,
            CreateUserChecklistCommand command,
            List<Long> itemIds) {
        UserChecklist checklist = checklistRepository.save(
                UserChecklist.create(memberId, command.name(), command.stage()));
        checklistRepository.replaceItems(checklist.getId(), itemIds);
        return checklist;
    }

    private List<Long> validateUpdateItems(UserChecklist checklist, List<Long> requestedItemIds) {
        Set<Long> existingIds = checklistRepository.findItems(checklist.getId()).stream()
                .map(UserChecklistItem::getSystemCheckItemId)
                .collect(Collectors.toSet());
        List<Long> requestedIds = requireDistinctIds(requestedItemIds, false);
        validateStageAndActive(checklist.getStage(), findRequestedItems(requestedIds), existingIds);
        validateRequiredCoreItems(checklist.getStage(), existingIds, requestedIds);
        requireItemCount(requestedIds);
        return requestedIds;
    }

    private void validateRequiredCoreItems(
            Stage stage,
            Set<Long> existingIds,
            List<Long> requestedIds) {
        Set<Long> requiredCoreIds = systemItemRepository.findAllByIds(List.copyOf(existingIds)).stream()
                .filter(item -> item.getItemType() == ItemType.CORE)
                .map(SystemCheckItem::getId)
                .collect(Collectors.toSet());
        systemItemRepository.findActiveCoreByStage(stage).stream()
                .map(SystemCheckItem::getId)
                .forEach(requiredCoreIds::add);
        if (!requestedIds.containsAll(requiredCoreIds)) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_CORE_ITEM_REQUIRED,
                    "핵심 항목은 제거할 수 없습니다.");
        }
    }

    private UserChecklist getOwnedChecklist(Long memberId, Long checklistId) {
        return checklistRepository.findActiveByIdAndMemberId(checklistId, memberId)
                .orElseThrow(this::checklistNotFound);
    }

    private UserChecklist getOwnedChecklistForUpdate(Long memberId, Long checklistId) {
        return checklistRepository.findActiveByIdAndMemberIdForUpdate(checklistId, memberId)
                .orElseThrow(this::checklistNotFound);
    }

    private BusinessException checklistNotFound() {
        return new BusinessException(
                DomainErrorCode.CHECKLIST_NOT_FOUND,
                "소유한 활성 체크리스트를 찾을 수 없습니다.");
    }

    private List<Long> requireDistinctIds(List<Long> ids, boolean allowEmpty) {
        if (ids == null
                || (!allowEmpty && ids.isEmpty())
                || ids.size() > MAX_ITEM_COUNT
                || ids.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "항목은 1개 이상 100개 이하여야 합니다.");
        }
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        if (distinctIds.size() != ids.size()) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "같은 항목을 중복할 수 없습니다.");
        }
        return List.copyOf(distinctIds);
    }

    private List<SystemCheckItem> findRequestedItems(List<Long> ids) {
        List<SystemCheckItem> items = systemItemRepository.findAllByIds(ids);
        if (items.size() != ids.size()) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEM_NOT_FOUND,
                    "시스템 체크 항목을 찾을 수 없습니다.");
        }
        return items;
    }

    private void validateStageAndActive(
            Stage stage,
            List<SystemCheckItem> items,
            Set<Long> existingIds) {
        for (SystemCheckItem item : items) {
            validateSameStage(stage, item);
            validateActiveOrExisting(existingIds, item);
        }
    }

    private void validateSameStage(Stage stage, SystemCheckItem item) {
        if (item.getStage() != stage) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEM_STAGE_MISMATCH,
                    "다른 단계의 항목을 추가할 수 없습니다.");
        }
    }

    private void validateActiveOrExisting(Set<Long> existingIds, SystemCheckItem item) {
        if (!item.isActive() && !existingIds.contains(item.getId())) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_INACTIVE_ITEM_NOT_ALLOWED,
                    "비활성 항목을 새로 추가할 수 없습니다.");
        }
    }

    private void requireItemCount(List<Long> itemIds) {
        if (itemIds.isEmpty() || itemIds.size() > MAX_ITEM_COUNT) {
            throw new BusinessException(
                    DomainErrorCode.CHECKLIST_ITEMS_INVALID,
                    "최종 항목은 1개 이상 100개 이하여야 합니다.");
        }
    }

    private UserChecklistDetailResult createDetailResult(UserChecklist checklist) {
        List<UserChecklistItem> checklistItems = checklistRepository.findItems(checklist.getId());
        List<Long> systemIds = checklistItems.stream()
                .map(UserChecklistItem::getSystemCheckItemId)
                .toList();
        Map<Long, SystemCheckItem> systemItemsById = new HashMap<>();
        systemItemRepository.findAllByIds(systemIds)
                .forEach(item -> systemItemsById.put(item.getId(), item));
        return UserChecklistDetailResult.from(checklist, checklistItems, systemItemsById);
    }
}
