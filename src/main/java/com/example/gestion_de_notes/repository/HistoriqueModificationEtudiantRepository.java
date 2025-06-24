package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.HistoriqueModificationEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueModificationEtudiantRepository extends JpaRepository<HistoriqueModificationEtudiant, Long> {
    
    @Query("SELECT h FROM HistoriqueModificationEtudiant h WHERE h.etudiant.id = :etudiantId ORDER BY h.dateModification DESC")
    List<HistoriqueModificationEtudiant> findByEtudiantIdOrderByDateDesc(@Param("etudiantId") Long etudiantId);
    
    @Query("SELECT h FROM HistoriqueModificationEtudiant h WHERE h.etudiant.idEtudiant = :etudiantId ORDER BY h.dateModification DESC")
    List<HistoriqueModificationEtudiant> findByEtudiantIdEtudiantOrderByDateModificationDesc(@Param("etudiantId") Long etudiantId);
    
    @Query("SELECT h FROM HistoriqueModificationEtudiant h WHERE h.utilisateurModification.id = :utilisateurId ORDER BY h.dateModification DESC")
    List<HistoriqueModificationEtudiant> findByUtilisateurIdOrderByDateDesc(@Param("utilisateurId") Long utilisateurId);
    
    @Query("SELECT h FROM HistoriqueModificationEtudiant h WHERE h.dateModification BETWEEN :debut AND :fin ORDER BY h.dateModification DESC")
    List<HistoriqueModificationEtudiant> findByDateModificationBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
