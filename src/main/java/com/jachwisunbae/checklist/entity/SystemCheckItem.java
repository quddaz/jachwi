package com.jachwisunbae.checklist.entity;

import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonBlank;
import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonNull;

import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.DomainErrorCode;

import lombok.Getter;

@Getter
public class SystemCheckItem {

    private final Long id;
    private final Stage stage;
    private final ItemType itemType;
    private final String question;
    private final String guide;
    private boolean active;

    private SystemCheckItem(
            Long id,
            Stage stage,
            ItemType itemType,
            String question,
            String guide,
            boolean active) {
        this.id = id;
        this.stage = stage;
        this.itemType = itemType;
        this.question = question;
        this.guide = guide;
        this.active = active;
    }

    public static SystemCheckItem create(
            Stage stage,
            ItemType itemType,
            String question,
            String guide) {
        return new SystemCheckItem(
                null,
                requireNonNull(
                        stage,
                        DomainErrorCode.SYSTEM_CHECK_ITEM_STAGE_REQUIRED,
                        "체크 단계는 필수입니다."),
                requireNonNull(
                        itemType,
                        DomainErrorCode.SYSTEM_CHECK_ITEM_TYPE_REQUIRED,
                        "항목 유형은 필수입니다."),
                requireNonBlank(
                        question,
                        DomainErrorCode.SYSTEM_CHECK_ITEM_QUESTION_INVALID,
                        "질문은 필수입니다."),
                guide,
                true);
    }

    public static SystemCheckItem restore(
            Long id,
            Stage stage,
            ItemType itemType,
            String question,
            String guide,
            boolean active) {
        return new SystemCheckItem(
                id,
                stage,
                itemType,
                question,
                guide,
                active);
    }

    public void deactivate() {
        this.active = false;
    }
}
