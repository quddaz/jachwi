package com.jachwisunbae.checklist.entity;

import static com.jachwisunbae.common.validation.DomainPreconditions.require;
import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonBlank;
import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonNull;

import java.time.LocalDateTime;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.DomainErrorCode;

import lombok.Getter;

@Getter
public class UserChecklist {

    private final Long id;
    private final Long memberId;
    private String name;
    private final Stage stage;
    private LocalDateTime deletedAt;

    private UserChecklist(
            Long id,
            Long memberId,
            String name,
            Stage stage,
            LocalDateTime deletedAt) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.stage = stage;
        this.deletedAt = deletedAt;
    }

    public static UserChecklist create(Long memberId, String name, Stage stage) {
        String normalizedName = requireNonBlank(
                name,
                DomainErrorCode.USER_CHECKLIST_NAME_INVALID,
                "체크리스트 이름은 필수입니다.")
                .trim();
        require(
                normalizedName.length() <= 50,
                DomainErrorCode.USER_CHECKLIST_NAME_INVALID,
                "체크리스트 이름은 50자 이하여야 합니다.");

        return new UserChecklist(
                null,
                requireNonNull(
                        memberId,
                        DomainErrorCode.USER_CHECKLIST_MEMBER_REQUIRED,
                        "체크리스트 소유 회원은 필수입니다."),
                normalizedName,
                requireNonNull(
                        stage,
                        DomainErrorCode.USER_CHECKLIST_STAGE_REQUIRED,
                        "체크리스트 단계는 필수입니다."),
                null);
    }

    public static UserChecklist restore(
            Long id,
            Long memberId,
            String name,
            Stage stage,
            LocalDateTime deletedAt) {
        return new UserChecklist(id, memberId, name, stage, deletedAt);
    }

    public void rename(String name) {
        String normalizedName = requireNonBlank(
                name,
                DomainErrorCode.USER_CHECKLIST_NAME_INVALID,
                "체크리스트 이름은 필수입니다.")
                .trim();
        require(
                normalizedName.length() <= 50,
                DomainErrorCode.USER_CHECKLIST_NAME_INVALID,
                "체크리스트 이름은 50자 이하여야 합니다.");
        this.name = normalizedName;
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = requireNonNull(
                deletedAt,
                DomainErrorCode.USER_CHECKLIST_DELETED_AT_REQUIRED,
                "체크리스트 삭제 시각은 필수입니다.");
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
