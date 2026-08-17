package com.jachwisunbae.property.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.jachwisunbae.property.entity.Property;
import com.jachwisunbae.property.repository.projection.PropertyWithProgress;

public interface PropertyRepository {

    boolean lockMember(Long memberId);

    long countByMemberId(Long memberId);

    long countByMemberIdAndQuery(Long memberId, String query);

    Property save(Property property);

    List<PropertyWithProgress> findPageByMemberId(
            Long memberId,
            String query,
            int size,
            long offset);

    Optional<PropertyWithProgress> findByIdAndMemberId(Long propertyId, Long memberId);

    int update(Property property);

    int deleteByIdAndMemberId(Long propertyId, Long memberId);

    int touch(Long propertyId, Long memberId, LocalDateTime activityAt);
}
