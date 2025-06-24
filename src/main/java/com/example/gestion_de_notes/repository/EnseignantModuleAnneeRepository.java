package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.EnseignantModuleAnnee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnseignantModuleAnneeRepository extends JpaRepository<EnseignantModuleAnnee, Long> {
    
    @Query("SELECT ema FROM EnseignantModuleAnnee ema WHERE ema.enseignant.id = :enseignantId AND ema.anneeUniversitaire = :annee")
    List<EnseignantModuleAnnee> findByEnseignantAndAnnee(@Param("enseignantId") Long enseignantId, @Param("annee") String annee);
    
    @Query("SELECT ema FROM EnseignantModuleAnnee ema WHERE ema.module.id = :moduleId AND ema.anneeUniversitaire = :annee")
    List<EnseignantModuleAnnee> findByModuleAndAnnee(@Param("moduleId") Long moduleId, @Param("annee") String annee);
    
    @Query("SELECT ema FROM EnseignantModuleAnnee ema WHERE ema.element.id = :elementId AND ema.anneeUniversitaire = :annee")
    List<EnseignantModuleAnnee> findByElementAndAnnee(@Param("elementId") Long elementId, @Param("annee") String annee);
    
    @Query("SELECT ema FROM EnseignantModuleAnnee ema WHERE ema.anneeUniversitaire = :annee")
    List<EnseignantModuleAnnee> findByAnneeUniversitaire(@Param("annee") String annee);
    
    @Query("SELECT ema FROM EnseignantModuleAnnee ema WHERE ema.enseignant.id = :enseignantId AND ema.module.id = :moduleId AND ema.anneeUniversitaire = :annee")
    List<EnseignantModuleAnnee> findByEnseignantModuleAndAnnee(@Param("enseignantId") Long enseignantId, @Param("moduleId") Long moduleId, @Param("annee") String annee);
}
