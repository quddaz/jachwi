package com.jachwisunbae.property.memo.repository;

import java.util.List;

import com.jachwisunbae.property.memo.entity.PropertyMemo;
import com.jachwisunbae.property.memo.entity.PropertyMemoItem;

public record PropertyMemoSnapshot(PropertyMemo memo, List<PropertyMemoItem> items) {
}
