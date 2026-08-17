package com.jachwisunbae.checklist.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.entity.UserChecklistItem;
import com.jachwisunbae.checklist.service.policy.UserChecklistPolicy;
import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.repository.UserChecklistRepository;
import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UpdateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.service.dto.UserChecklistSummaryResult;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.checklist.service.validation.UserChecklistValidator;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class UserChecklistService {

    private final UserChecklistRepository checklistRepository;
    private final SystemCheckItemRepository systemItemRepository;
    private final MemberRepository memberRepository;
    private final UserChecklistValidator validator;
    private final UserChecklistPolicy policy;

    public UserChecklistService(
            UserChecklistRepository checklistRepository,
            SystemCheckItemRepository systemItemRepository,
            MemberRepository memberRepository,
            UserChecklistValidator validator,
            UserChecklistPolicy policy) {
        this.checklistRepository = checklistRepository;
        this.systemItemRepository = systemItemRepository;
        this.memberRepository = memberRepository;
        this.validator = validator;
        this.policy = policy;
    }

    @Transactional
    public UserChecklistDetailResult create(Long memberId, CreateUserChecklistCommand command) {
        // 요청 항목을 검증한 뒤 같은 단계의 활성 CORE를 자동 포함하고 최종 표시 순서로 저장한다.
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
        // 체크리스트를 잠근 뒤 기존 비활성 항목의 유지 여부와 항목 구성을 검증한다.
        // 검증을 통과하면 이름과 전체 항목 순서를 한 트랜잭션으로 교체한다.
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
        List<Long> requestedIds = validator.validateItemIds(command.checkItemIds(), true);
        validator.validateItems(command.stage(), findRequestedItems(requestedIds), Set.of());
        List<Long> finalIds = policy.resolveCreateItemIds(
                requestedIds,
                systemItemRepository.findActiveCoreByStage(command.stage()));
        validator.validateFinalItemCount(finalIds);
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
        List<Long> requestedIds = validator.validateItemIds(requestedItemIds, false);
        validator.validateItems(checklist.getStage(), findRequestedItems(requestedIds), existingIds);
        validator.validateFinalItemCount(requestedIds);
        return requestedIds;
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

    private List<SystemCheckItem> findRequestedItems(List<Long> ids) {
        List<SystemCheckItem> items = systemItemRepository.findAllByIds(ids);
        validator.validateExistingItems(ids, items);
        return items;
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
