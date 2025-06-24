package com.example.gestion_de_notes.entity;

public enum TypeInscription {
    INSCRIPTION("Inscription"),
    REINSCRIPTION("Réinscription");
    
    private final String displayName;
    
    TypeInscription(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
