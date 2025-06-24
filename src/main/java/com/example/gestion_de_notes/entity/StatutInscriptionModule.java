package com.example.gestion_de_notes.entity;

public enum StatutInscriptionModule {
    INSCRIT("Inscrit"),
    AJOURNE("Ajourné"),
    VALIDE("Validé");
    
    private final String displayName;
    
    StatutInscriptionModule(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
