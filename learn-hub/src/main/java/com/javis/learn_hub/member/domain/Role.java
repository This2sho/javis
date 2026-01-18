package com.javis.learn_hub.member.domain;

public enum Role {
    USER(1),
    ADMIN(10);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public boolean hasAuthority(Role requiredRole) {
        return this.level >= requiredRole.level;
    }
}
