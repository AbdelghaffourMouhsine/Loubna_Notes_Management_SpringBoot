package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.HistoriqueConnexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueConnexionRepository extends JpaRepository<HistoriqueConnexion, Long> {
    
    @Query("SELECT h FROM HistoriqueConnexion h WHERE h.utilisateur.id = :utilisateurId ORDER BY h.dateConnexion DESC")
    List<HistoriqueConnexion> findByUtilisateurIdOrderByDateDesc(@Param("utilisateurId") Long utilisateurId);
    
    @Query("SELECT h FROM HistoriqueConnexion h WHERE h.succes = :succes ORDER BY h.dateConnexion DESC")
    List<HistoriqueConnexion> findBySucces(@Param("succes") Boolean succes);
    
    @Query("SELECT h FROM HistoriqueConnexion h WHERE h.adresseIp = :ip ORDER BY h.dateConnexion DESC")
    List<HistoriqueConnexion> findByAdresseIp(@Param("ip") String adresseIp);
    
    @Query("SELECT h FROM HistoriqueConnexion h WHERE h.dateConnexion BETWEEN :debut AND :fin ORDER BY h.dateConnexion DESC")
    List<HistoriqueConnexion> findByDateConnexionBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
