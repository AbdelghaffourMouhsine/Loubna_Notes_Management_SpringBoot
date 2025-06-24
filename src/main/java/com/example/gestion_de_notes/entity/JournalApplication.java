package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "journal_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_journal")
    private Long idJournal;
    
    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private CompteUtilisateur utilisateur;
    
    @NotBlank(message = "L'action effectuée est obligatoire")
    @Column(name = "action_effectuee", nullable = false)
    private String actionEffectuee;
    
    @Column(name = "details", length = 1000)
    private String details;
    
    @Column(name = "date_action")
    private LocalDateTime dateAction;
    
    @Column(name = "adresse_ip")
    private String adresseIp;
    
    @PrePersist
    protected void onCreate() {
        dateAction = LocalDateTime.now();
    }
}
