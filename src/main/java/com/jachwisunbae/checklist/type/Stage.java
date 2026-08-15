package com.jachwisunbae.checklist.type;

public enum Stage {
    ONLINE_PHONE("온라인·전화"),
    ON_SITE("현장"),
    PRE_CONTRACT("계약 전");

    private final String displayName;


    Stage(String displayName) {
        this.displayName = displayName;
    }

}
