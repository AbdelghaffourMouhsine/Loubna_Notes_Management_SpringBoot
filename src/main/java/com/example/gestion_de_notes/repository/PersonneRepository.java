package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonneRepository extends JpaRepository<Personne, Long> {
    
    Optional<Personne> findByCin(String cin);
    
    boolean existsByCin(String cin);
    
    @Query("SELECT p FROM Personne p WHERE " +
           "LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.cin) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Personne> findByNomOrPrenomOrCinContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM Personne p WHERE " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%')) OR :nom IS NULL) AND " +
           "(LOWER(p.cin) LIKE LOWER(CONCAT('%', :cin, '%')) OR :cin IS NULL)")
    List<Personne> findByNomAndCin(@Param("nom") String nom, @Param("cin") String cin);
}
