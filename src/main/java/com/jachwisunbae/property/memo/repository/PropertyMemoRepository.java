package com.jachwisunbae.property.memo.repository;

import java.util.List;
import java.util.Optional;

import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand.Item;

public interface PropertyMemoRepository {

    Optional<PropertyMemoSnapshot> findByPropertyId(Long propertyId);

    Long saveRoot(Long propertyId, String freeMemo);

    void deleteItems(Long propertyMemoId);

    void insertItems(Long propertyMemoId, List<Item> items);
}
