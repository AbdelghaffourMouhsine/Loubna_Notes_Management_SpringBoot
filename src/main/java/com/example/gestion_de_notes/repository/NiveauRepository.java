package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Niveau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NiveauRepository extends JpaRepository<Niveau, Long> {
    
    Optional<Niveau> findByAlias(String alias);
    
    List<Niveau> findByLibelleContainingIgnoreCase(String libelle);
    
    @Query("SELECT n FROM Niveau n WHERE n.niveauSuivant.id = :niveauId")
    List<Niveau> findNiveauxPrecedents(@Param("niveauId") Long niveauId);
    
    @Query("SELECT n FROM Niveau n JOIN n.filieres f WHERE f.id = :filiereId")
    List<Niveau> findByFiliereId(@Param("filiereId") Long filiereId);
    
    boolean existsByAlias(String alias);
    
    boolean existsByIdNiveau(Long idNiveau);
}
