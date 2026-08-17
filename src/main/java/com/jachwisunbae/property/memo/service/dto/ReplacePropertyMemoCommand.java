package com.jachwisunbae.property.memo.service.dto;

import java.util.List;

public record ReplacePropertyMemoCommand(List<PropertyMemoItemCommand> items, String freeMemo) {
}
