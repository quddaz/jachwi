package com.jachwisunbae.property.memo.entity;

import lombok.Getter;

@Getter
public class PropertyMemoItem {

    private final Long id;
    private final Long propertyMemoId;
    private final String label;
    private final String content;
    private final int displayOrder;

    private PropertyMemoItem(
            Long id,
            Long propertyMemoId,
            String label,
            String content,
            int displayOrder) {
        this.id = id;
        this.propertyMemoId = propertyMemoId;
        this.label = label;
        this.content = content;
        this.displayOrder = displayOrder;
    }

    public static PropertyMemoItem restore(
            Long id,
            Long propertyMemoId,
            String label,
            String content,
            int displayOrder) {
        return new PropertyMemoItem(id, propertyMemoId, label, content, displayOrder);
    }
}
