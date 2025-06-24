package com.example.gestion_de_notes.entity;

public enum Session {
    NORMALE("Normale"),
    RATTRAPAGE("Rattrapage");
    
    private final String displayName;
    
    Session(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
