package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.ParametreValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametreValidationRepository extends JpaRepository<ParametreValidation, Long> {
    
    @Query("SELECT pv FROM ParametreValidation pv WHERE pv.filiere.id = :filiereId AND pv.niveau.id = :niveauId")
    Optional<ParametreValidation> findByFiliereAndNiveau(@Param("filiereId") Long filiereId, @Param("niveauId") Long niveauId);
    
    @Query("SELECT pv FROM ParametreValidation pv WHERE pv.filiere.idFiliere = :filiereId")
    List<ParametreValidation> findByFiliereId(@Param("filiereId") Long filiereId);
    
    @Query("SELECT pv FROM ParametreValidation pv WHERE pv.niveau.idNiveau = :niveauId")
    List<ParametreValidation> findByNiveauId(@Param("niveauId") Long niveauId);
    
    @Query("SELECT pv FROM ParametreValidation pv WHERE pv.filiere.alias = :filiereAlias")
    List<ParametreValidation> findByFiliereAlias(@Param("filiereAlias") String filiereAlias);
    
    @Query("SELECT pv FROM ParametreValidation pv WHERE pv.niveau.alias = :niveauAlias")
    List<ParametreValidation> findByNiveauAlias(@Param("niveauAlias") String niveauAlias);
}
