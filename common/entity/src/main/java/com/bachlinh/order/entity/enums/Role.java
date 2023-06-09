package com.bachlinh.order.entity.enums;

public enum Role {
    CUSTOMER,
    ADMIN;

    public static Role of(String roleName) {
        for (Role r : values()) {
            if (r.name().equals(roleName.toUpperCase())) {
                return r;
            }
        }
        return null;
    }
}