package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Inscription;
import com.example.gestion_de_notes.entity.TypeInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    
    @Query("SELECT i FROM Inscription i WHERE i.etudiant.id = :etudiantId AND i.anneeUniversitaire = :annee")
    List<Inscription> findByEtudiantAndAnnee(@Param("etudiantId") Long etudiantId, @Param("annee") String annee);
    
    @Query("SELECT i FROM Inscription i WHERE i.niveau.id = :niveauId AND i.anneeUniversitaire = :annee")
    List<Inscription> findByNiveauAndAnnee(@Param("niveauId") Long niveauId, @Param("annee") String annee);
    
    @Query("SELECT i FROM Inscription i WHERE i.etudiant.id = :etudiantId AND i.niveau.id = :niveauId AND i.anneeUniversitaire = :annee")
    Optional<Inscription> findByEtudiantNiveauAndAnnee(@Param("etudiantId") Long etudiantId, @Param("niveauId") Long niveauId, @Param("annee") String annee);
    
    List<Inscription> findByTypeInscriptionAndAnneeUniversitaire(TypeInscription type, String anneeUniversitaire);
    
    @Query("SELECT i FROM Inscription i WHERE i.etudiant.cne = :cne")
    List<Inscription> findByEtudiantCne(@Param("cne") String cne);
    
    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.niveau.id = :niveauId AND i.anneeUniversitaire = :annee")
    long countByNiveauAndAnnee(@Param("niveauId") Long niveauId, @Param("annee") String annee);
    
    @Query("SELECT i FROM Inscription i WHERE i.niveau = :niveau AND i.anneeUniversitaire = :anneeUniversitaire")
    List<Inscription> findByNiveauAndAnneeUniversitaire(@Param("niveau") com.example.gestion_de_notes.entity.Niveau niveau,
                                                       @Param("anneeUniversitaire") String anneeUniversitaire);
}
