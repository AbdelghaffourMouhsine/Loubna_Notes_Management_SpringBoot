package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    
    Optional<Filiere> findByAlias(String alias);
    
    List<Filiere> findByIntituleContainingIgnoreCase(String intitule);
    
    @Query("SELECT f FROM Filiere f WHERE f.coordonnateur.id = :coordonnateurId")
    List<Filiere> findByCoordonnateur(@Param("coordonnateurId") Long coordonnateurId);
    
    @Query("SELECT f FROM Filiere f WHERE f.anneeAccreditation <= :annee AND (f.anneeFinAccreditation IS NULL OR f.anneeFinAccreditation >= :annee)")
    List<Filiere> findFilieresByAnnee(@Param("annee") Integer annee);
    
    boolean existsByAlias(String alias);
}
