package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.JournalApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalApplicationRepository extends JpaRepository<JournalApplication, Long> {
    
    @Query("SELECT j FROM JournalApplication j WHERE j.utilisateur.id = :utilisateurId ORDER BY j.dateAction DESC")
    List<JournalApplication> findByUtilisateurIdOrderByDateDesc(@Param("utilisateurId") Long utilisateurId);
    
    @Query("SELECT j FROM JournalApplication j WHERE j.actionEffectuee LIKE %:action% ORDER BY j.dateAction DESC")
    List<JournalApplication> findByActionContaining(@Param("action") String action);
    
    @Query("SELECT j FROM JournalApplication j WHERE j.dateAction BETWEEN :debut AND :fin ORDER BY j.dateAction DESC")
    List<JournalApplication> findByDateActionBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT j FROM JournalApplication j WHERE j.adresseIp = :ip ORDER BY j.dateAction DESC")
    List<JournalApplication> findByAdresseIp(@Param("ip") String adresseIp);
}
