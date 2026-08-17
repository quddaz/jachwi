package com.jachwisunbae.property.memo.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemCommand;
import com.jachwisunbae.property.memo.entity.PropertyMemoSnapshot;

public interface PropertyMemoRepository {

    Optional<PropertyMemoSnapshot> findByPropertyId(Long propertyId);

    Long saveRoot(Long propertyId, String freeMemo);

    void deleteItems(Long propertyMemoId);

    void insertItems(Long propertyMemoId, List<PropertyMemoItemCommand> items);
}
