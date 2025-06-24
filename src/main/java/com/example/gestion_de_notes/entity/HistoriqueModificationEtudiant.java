package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_modification_etudiant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueModificationEtudiant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistorique;
    
    @NotNull(message = "L'étudiant est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etudiant", nullable = false)
    private Etudiant etudiant;
    
    @Column(name = "ancien_cne")
    private String ancienCne;
    
    @Column(name = "ancien_nom")
    private String ancienNom;
    
    @Column(name = "ancien_prenom")
    private String ancienPrenom;
    
    @Column(name = "nouveau_cne")
    private String nouveauCne;
    
    @Column(name = "nouveau_nom")
    private String nouveauNom;
    
    @Column(name = "nouveau_prenom")
    private String nouveauPrenom;
    
    @NotNull(message = "L'utilisateur qui a effectué la modification est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_modification", nullable = false)
    private CompteUtilisateur utilisateurModification;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateModification = LocalDateTime.now();
    }
}
