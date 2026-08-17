package com.jachwisunbae.property.memo.entity;

import java.util.List;

public record PropertyMemoSnapshot(PropertyMemo memo, List<PropertyMemoItem> items) {
}
