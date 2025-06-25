package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.InscriptionModule;
import com.example.gestion_de_notes.entity.StatutInscriptionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionModuleRepository extends JpaRepository<InscriptionModule, Long> {
    
    @Query("SELECT im FROM InscriptionModule im WHERE im.inscription.id = :inscriptionId")
    List<InscriptionModule> findByInscriptionId(@Param("inscriptionId") Long inscriptionId);
    
    @Query("SELECT im FROM InscriptionModule im WHERE im.module.id = :moduleId")
    List<InscriptionModule> findByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT im FROM InscriptionModule im WHERE im.inscription.etudiant.id = :etudiantId AND im.module.id = :moduleId")
    Optional<InscriptionModule> findByEtudiantAndModule(@Param("etudiantId") Long etudiantId, @Param("moduleId") Long moduleId);
    
    List<InscriptionModule> findByStatut(StatutInscriptionModule statut);
    
    @Query("SELECT im FROM InscriptionModule im WHERE im.inscription.etudiant.id = :etudiantId AND im.statut = :statut")
    List<InscriptionModule> findByEtudiantAndStatut(@Param("etudiantId") Long etudiantId, @Param("statut") StatutInscriptionModule statut);
    
    List<InscriptionModule> findByInscription(com.example.gestion_de_notes.entity.Inscription inscription);
    
    Optional<InscriptionModule> findByInscriptionAndModule(com.example.gestion_de_notes.entity.Inscription inscription, 
                                                          com.example.gestion_de_notes.entity.Module module);
    
    @Query("SELECT im FROM InscriptionModule im WHERE im.module = :module AND im.inscription.anneeUniversitaire = :anneeUniversitaire")
    List<InscriptionModule> findByModuleAndAnneeUniversitaire(@Param("module") com.example.gestion_de_notes.entity.Module module,
                                                             @Param("anneeUniversitaire") String anneeUniversitaire);
}
