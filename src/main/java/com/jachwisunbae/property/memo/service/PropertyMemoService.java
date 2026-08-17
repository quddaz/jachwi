package com.jachwisunbae.property.memo.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.memo.repository.PropertyMemoRepository;
import com.jachwisunbae.property.memo.entity.PropertyMemoSnapshot;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoResult;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemResult;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.validation.PropertyMemoValidator;
import com.jachwisunbae.property.repository.PropertyRepository;

@Service
@Transactional(readOnly = true)
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

    public PropertyMemoResult get(Long memberId, Long propertyId) {
        validateOwnership(memberId, propertyId);
        Optional<PropertyMemoSnapshot> snapshot = memoRepository.findByPropertyId(propertyId);
        if (snapshot.isEmpty()) {
            return PropertyMemoResult.empty();
        }
        return toResult(snapshot.get());
    }

    @Transactional
    public PropertyMemoResult replace(
            Long memberId,
            Long propertyId,
            ReplacePropertyMemoCommand command) {
        // 자유 메모 루트를 저장한 뒤 구조화 항목을 요청 배열로 전체 교체한다.
        // 소유권 확인부터 활동 시각 갱신까지 한 트랜잭션으로 묶어 부분 저장을 방지한다.
        validateOwnership(memberId, propertyId);
        ReplacePropertyMemoCommand validated = validator.validate(command);
        Long memoId = memoRepository.saveRoot(propertyId, validated.freeMemo());
        memoRepository.deleteItems(memoId);
        memoRepository.insertItems(memoId, validated.items());
        propertyRepository.touch(propertyId, memberId, LocalDateTime.now(clock));
        Optional<PropertyMemoSnapshot> saved = memoRepository.findByPropertyId(propertyId);
        if (saved.isEmpty()) {
            throw new IllegalStateException("저장한 매물 메모를 찾을 수 없습니다.");
        }
        return toResult(saved.get());
    }

    private void validateOwnership(Long memberId, Long propertyId) {
        if (propertyRepository.findByIdAndMemberId(propertyId, memberId).isEmpty()) {
            throw new BusinessException(DomainErrorCode.PROPERTY_NOT_FOUND, "매물을 찾을 수 없습니다.");
        }
    }

    private PropertyMemoResult toResult(PropertyMemoSnapshot snapshot) {
        return new PropertyMemoResult(
                toItemResults(snapshot),
                snapshot.memo().getFreeMemo(),
                snapshot.memo().getUpdatedAt());
    }

    private List<PropertyMemoItemResult> toItemResults(PropertyMemoSnapshot snapshot) {
        List<PropertyMemoItemResult> results = new ArrayList<>();
        for (var item : snapshot.items()) {
            results.add(new PropertyMemoItemResult(
                    item.getLabel(), item.getContent(), item.getDisplayOrder()));
        }
        return List.copyOf(results);
    }
}
