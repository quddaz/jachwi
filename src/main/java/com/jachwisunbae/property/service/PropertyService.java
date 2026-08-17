package com.jachwisunbae.property.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.entity.Property;
import com.jachwisunbae.property.repository.PropertyRepository;
import com.jachwisunbae.property.repository.projection.PropertyWithProgress;
import com.jachwisunbae.property.service.dto.CreatePropertyCommand;
import com.jachwisunbae.property.service.dto.PropertyPageResult;
import com.jachwisunbae.property.service.dto.PropertyResult;
import com.jachwisunbae.property.service.dto.UpdatePropertyCommand;
import com.jachwisunbae.property.service.policy.PropertyPolicy;
import com.jachwisunbae.property.service.validation.PropertyValidator;

@Service
public class PropertyService {

    private final PropertyRepository repository;
    private final PropertyValidator validator;
    private final PropertyPolicy policy;
    private final Clock clock;

    public PropertyService(
            PropertyRepository repository,
            PropertyValidator validator,
            PropertyPolicy policy,
            Clock clock) {
        this.repository = repository;
        this.validator = validator;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional
    public PropertyResult create(Long memberId, CreatePropertyCommand command) {
        String normalizedName = validator.validate(command);
        lockMember(memberId);
        policy.validateCreationAllowed(repository.countByMemberId(memberId));

        Property saved = repository.save(Property.create(
                memberId,
                normalizedName,
                command.depositAmount(),
                command.monthlyRentAmount(),
                command.maintenanceFeeAmount(),
                command.address(),
                command.discoverySource(),
                LocalDateTime.now(clock)));
        return findOwned(saved.getId(), memberId);
    }

    @Transactional(readOnly = true)
    public PropertyPageResult findAll(Long memberId, String query, int page, int size) {
        validator.validatePage(page, size);
        String normalizedQuery = validator.normalizeQuery(query);
        long totalElements = repository.countByMemberIdAndQuery(memberId, normalizedQuery);
        List<PropertyResult> content = new ArrayList<>();
        for (PropertyWithProgress row : repository.findPageByMemberId(
                memberId, normalizedQuery, size, (long) page * size)) {
            content.add(toResult(row));
        }
        return PropertyPageResult.of(content, page, size, totalElements);
    }

    @Transactional(readOnly = true)
    public PropertyResult findOne(Long memberId, Long propertyId) {
        return findOwned(propertyId, memberId);
    }

    @Transactional
    public PropertyResult update(
            Long memberId,
            Long propertyId,
            UpdatePropertyCommand command) {
        validator.validate(command);
        Property property = findOwnedProperty(propertyId, memberId).property();
        property.update(
                command.name().present()
                        ? validator.normalizeName(command.name().value())
                        : property.getName(),
                command.depositAmount().apply(property.getDepositAmount()),
                command.monthlyRentAmount().apply(property.getMonthlyRentAmount()),
                command.maintenanceFeeAmount().apply(property.getMaintenanceFeeAmount()),
                command.address().apply(property.getAddress()),
                command.discoverySource().apply(property.getDiscoverySource()),
                LocalDateTime.now(clock));
        repository.update(property);
        return findOwned(propertyId, memberId);
    }

    @Transactional
    public void delete(Long memberId, Long propertyId) {
        if (repository.deleteByIdAndMemberId(propertyId, memberId) == 0) {
            throw notFound();
        }
    }

    private void lockMember(Long memberId) {
        if (!repository.lockMember(memberId)) {
            throw new BusinessException(DomainErrorCode.MEMBER_NOT_FOUND, "회원을 찾을 수 없습니다.");
        }
    }

    private PropertyResult findOwned(Long propertyId, Long memberId) {
        return toResult(findOwnedProperty(propertyId, memberId));
    }

    private PropertyWithProgress findOwnedProperty(Long propertyId, Long memberId) {
        return repository.findByIdAndMemberId(propertyId, memberId).orElseThrow(this::notFound);
    }

    private PropertyResult toResult(PropertyWithProgress row) {
        return PropertyResult.from(
                row.property(), row.totalCount(), row.completedCount(), row.goodCount(),
                row.cautionCount(), row.unconfirmedCount());
    }

    private BusinessException notFound() {
        return new BusinessException(DomainErrorCode.PROPERTY_NOT_FOUND, "매물을 찾을 수 없습니다.");
    }
}
