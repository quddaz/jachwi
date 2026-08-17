package com.jachwisunbae.property.checklist.entity;

import com.jachwisunbae.checklist.type.Stage;

import lombok.Getter;

@Getter
public class PropertyChecklist {

    private final Long id;
    private final Long propertyId;
    private final Long sourceUserChecklistId;
    private final String name;
    private final Stage stage;

    private PropertyChecklist(
            Long id,
            Long propertyId,
            Long sourceUserChecklistId,
            String name,
            Stage stage) {
        this.id = id;
        this.propertyId = propertyId;
        this.sourceUserChecklistId = sourceUserChecklistId;
        this.name = name;
        this.stage = stage;
    }

    public static PropertyChecklist create(
            Long propertyId,
            Long sourceUserChecklistId,
            String name,
            Stage stage) {
        return new PropertyChecklist(null, propertyId, sourceUserChecklistId, name, stage);
    }

    public static PropertyChecklist restore(
            Long id,
            Long propertyId,
            Long sourceUserChecklistId,
            String name,
            Stage stage) {
        return new PropertyChecklist(id, propertyId, sourceUserChecklistId, name, stage);
    }
}
