package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "parametre_validation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametreValidation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Long idParametre;
    
    @NotNull(message = "La filière est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_filiere", nullable = false)
    private Filiere filiere;
    
    @NotNull(message = "Le niveau est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_niveau", nullable = false)
    private Niveau niveau;
    
    @NotNull(message = "Le seuil de validation normale est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil doit être supérieur ou égal à 0")
    @DecimalMax(value = "20.0", message = "Le seuil doit être inférieur ou égal à 20")
    @Column(name = "seuil_validation_normale", nullable = false, precision = 4, scale = 2)
    private BigDecimal seuilValidationNormale;
    
    @NotNull(message = "Le seuil de validation rattrapage est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil doit être supérieur ou égal à 0")
    @DecimalMax(value = "20.0", message = "Le seuil doit être inférieur ou égal à 20")
    @Column(name = "seuil_validation_rattrapage", nullable = false, precision = 4, scale = 2)
    private BigDecimal seuilValidationRattrapage;
}
