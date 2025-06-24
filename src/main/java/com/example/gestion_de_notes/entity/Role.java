package com.example.gestion_de_notes.entity;

public enum Role {
    ADMIN_USER("Administrateur Utilisateurs"),
    ADMIN_NOTES("Administrateur Notes"),
    ADMIN_SP("Administrateur Structures Pédagogiques");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
