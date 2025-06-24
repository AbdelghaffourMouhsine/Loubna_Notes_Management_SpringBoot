package com.example.gestion_de_notes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "formule_calcul")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormuleCalcul {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formule")
    private Long idFormule;
    
    @NotBlank(message = "Le type de formule est obligatoire")
    @Column(name = "type_formule", nullable = false)
    private String typeFormule;
    
    @NotBlank(message = "La formule Excel est obligatoire")
    @Column(name = "formule_excel", nullable = false, length = 1000)
    private String formuleExcel;
    
    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    @Column(name = "description", length = 500)
    private String description;
}
