package com.jachwisunbae.property.entity;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class Property {

    private final Long id;
    private final Long memberId;
    private String name;
    private Long depositAmount;
    private Long monthlyRentAmount;
    private Long maintenanceFeeAmount;
    private String address;
    private String discoverySource;
    private LocalDateTime lastActivityAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Property(
            Long id,
            Long memberId,
            String name,
            Long depositAmount,
            Long monthlyRentAmount,
            Long maintenanceFeeAmount,
            String address,
            String discoverySource,
            LocalDateTime lastActivityAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.depositAmount = depositAmount;
        this.monthlyRentAmount = monthlyRentAmount;
        this.maintenanceFeeAmount = maintenanceFeeAmount;
        this.address = address;
        this.discoverySource = discoverySource;
        this.lastActivityAt = lastActivityAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Property create(
            Long memberId,
            String name,
            Long depositAmount,
            Long monthlyRentAmount,
            Long maintenanceFeeAmount,
            String address,
            String discoverySource,
            LocalDateTime now) {
        return new Property(null, memberId, name, depositAmount, monthlyRentAmount,
                maintenanceFeeAmount, address, discoverySource, now, null, null);
    }

    public static Property restore(
            Long id,
            Long memberId,
            String name,
            Long depositAmount,
            Long monthlyRentAmount,
            Long maintenanceFeeAmount,
            String address,
            String discoverySource,
            LocalDateTime lastActivityAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new Property(id, memberId, name, depositAmount, monthlyRentAmount,
                maintenanceFeeAmount, address, discoverySource, lastActivityAt, createdAt, updatedAt);
    }

    public void update(
            String name,
            Long depositAmount,
            Long monthlyRentAmount,
            Long maintenanceFeeAmount,
            String address,
            String discoverySource,
            LocalDateTime now) {
        this.name = name;
        this.depositAmount = depositAmount;
        this.monthlyRentAmount = monthlyRentAmount;
        this.maintenanceFeeAmount = maintenanceFeeAmount;
        this.address = address;
        this.discoverySource = discoverySource;
        this.lastActivityAt = now;
    }
}
