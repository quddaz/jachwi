package com.jachwisunbae.property.memo.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand.Item;

@Component
public class PropertyMemoValidator {

    public ReplacePropertyMemoCommand validate(ReplacePropertyMemoCommand command) {
        if (command == null || command.items() == null || command.items().size() > 20) {
            throw invalid("구조화 메모는 최대 20개까지 저장할 수 있습니다.");
        }
        String freeMemo = command.freeMemo() == null ? "" : command.freeMemo();
        if (freeMemo.length() > 2000) {
            throw invalid("자유 메모는 2,000자 이하여야 합니다.");
        }
        List<Item> items = command.items().stream().map(this::validateItem).toList();
        return new ReplacePropertyMemoCommand(items, freeMemo);
    }

    private Item validateItem(Item item) {
        if (item == null || item.label() == null) {
            throw invalid("메모 항목명은 필수입니다.");
        }
        String label = item.label().trim();
        if (label.isEmpty() || label.length() > 30) {
            throw invalid("메모 항목명은 1자 이상 30자 이하여야 합니다.");
        }
        String content = item.content() == null ? "" : item.content();
        if (content.length() > 200) {
            throw invalid("메모 항목 내용은 200자 이하여야 합니다.");
        }
        return new Item(label, content);
    }

    private BusinessException invalid(String message) {
        return new BusinessException(DomainErrorCode.PROPERTY_MEMO_INVALID, message);
    }
}
