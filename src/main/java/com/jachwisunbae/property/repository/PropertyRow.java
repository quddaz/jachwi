package com.jachwisunbae.property.repository;

import com.jachwisunbae.property.entity.Property;

public record PropertyRow(Property property, long totalCount, long completedCount) {
}
