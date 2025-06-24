package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Long idNote;
    
    @NotNull(message = "L'étudiant est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etudiant", nullable = false)
    private Etudiant etudiant;
    
    @NotNull(message = "L'élément est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_element", nullable = false)
    private Element element;
    
    @NotNull(message = "La valeur de la note est obligatoire")
    @DecimalMin(value = "0.0", message = "La note doit être supérieure ou égale à 0")
    @DecimalMax(value = "20.0", message = "La note doit être inférieure ou égale à 20")
    @Column(name = "valeur_note", nullable = false, precision = 4, scale = 2)
    private BigDecimal valeurNote;
    
    @NotNull(message = "La session est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "session", nullable = false)
    private Session session;
    
    @NotBlank(message = "L'année universitaire est obligatoire")
    @Column(name = "annee_universitaire", nullable = false)
    private String anneeUniversitaire;
}
