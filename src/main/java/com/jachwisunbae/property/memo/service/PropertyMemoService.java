package com.jachwisunbae.property.memo.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.memo.repository.PropertyMemoRepository;
import com.jachwisunbae.property.memo.repository.PropertyMemoSnapshot;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoResult;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.validation.PropertyMemoValidator;
import com.jachwisunbae.property.repository.PropertyRepository;

@Service
public class PropertyMemoService {

    private final PropertyMemoRepository memoRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMemoValidator validator;
    private final Clock clock;

    public PropertyMemoService(
            PropertyMemoRepository memoRepository,
            PropertyRepository propertyRepository,
            PropertyMemoValidator validator,
            Clock clock) {
        this.memoRepository = memoRepository;
        this.propertyRepository = propertyRepository;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PropertyMemoResult get(Long memberId, Long propertyId) {
        validateOwnership(memberId, propertyId);
        return memoRepository.findByPropertyId(propertyId)
                .map(this::toResult)
                .orElseGet(PropertyMemoResult::empty);
    }

    @Transactional
    public PropertyMemoResult replace(
            Long memberId,
            Long propertyId,
            ReplacePropertyMemoCommand command) {
        validateOwnership(memberId, propertyId);
        ReplacePropertyMemoCommand validated = validator.validate(command);
        Long memoId = memoRepository.saveRoot(propertyId, validated.freeMemo());
        memoRepository.deleteItems(memoId);
        memoRepository.insertItems(memoId, validated.items());
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
        return memoRepository.findByPropertyId(propertyId)
                .map(this::toResult)
                .orElseThrow(() -> new IllegalStateException("저장한 매물 메모를 찾을 수 없습니다."));
    }

    private void validateOwnership(Long memberId, Long propertyId) {
        if (propertyRepository.findByIdAndMemberId(propertyId, memberId).isEmpty()) {
            throw new BusinessException(DomainErrorCode.PROPERTY_NOT_FOUND, "매물을 찾을 수 없습니다.");
        }
    }

    private PropertyMemoResult toResult(PropertyMemoSnapshot snapshot) {
        return new PropertyMemoResult(
                snapshot.items().stream()
                        .map(item -> new PropertyMemoResult.Item(
                                item.getLabel(), item.getContent(), item.getDisplayOrder()))
                        .toList(),
                snapshot.memo().getFreeMemo(),
                snapshot.memo().getUpdatedAt());
    }
}
