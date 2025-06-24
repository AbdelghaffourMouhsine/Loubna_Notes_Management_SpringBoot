package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_connexion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueConnexion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_connexion")
    private Long idConnexion;
    
    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private CompteUtilisateur utilisateur;
    
    @Column(name = "adresse_ip")
    private String adresseIp;
    
    @Column(name = "date_connexion")
    private LocalDateTime dateConnexion;
    
    @Column(name = "succes")
    private Boolean succes;
    
    @PrePersist
    protected void onCreate() {
        dateConnexion = LocalDateTime.now();
    }
}
