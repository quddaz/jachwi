package com.jachwisunbae.property.memo.service.validation;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemCommand;

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
        List<PropertyMemoItemCommand> items = validateItems(command.items());
        return new ReplacePropertyMemoCommand(items, freeMemo);
    }

    private List<PropertyMemoItemCommand> validateItems(
            List<PropertyMemoItemCommand> requestedItems) {
        List<PropertyMemoItemCommand> validated = new ArrayList<>();
        for (PropertyMemoItemCommand item : requestedItems) {
            validated.add(validateItem(item));
        }
        return List.copyOf(validated);
    }

    private PropertyMemoItemCommand validateItem(PropertyMemoItemCommand item) {
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
        return new PropertyMemoItemCommand(label, content);
    }

    private BusinessException invalid(String message) {
        return new BusinessException(DomainErrorCode.PROPERTY_MEMO_INVALID, message);
    }
}
