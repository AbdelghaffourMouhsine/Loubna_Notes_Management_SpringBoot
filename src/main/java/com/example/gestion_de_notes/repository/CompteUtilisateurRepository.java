package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompteUtilisateurRepository extends JpaRepository<CompteUtilisateur, Long> {
    
    Optional<CompteUtilisateur> findByLogin(String login);
    
    @Query("SELECT c FROM CompteUtilisateur c JOIN FETCH c.personne WHERE c.login = :login")
    Optional<CompteUtilisateur> findByLoginWithPersonne(@Param("login") String login);
    
    @Query("SELECT c FROM CompteUtilisateur c JOIN FETCH c.personne")
    List<CompteUtilisateur> findAllWithPersonnes();
    
    @Query("SELECT c FROM CompteUtilisateur c JOIN FETCH c.personne WHERE c.idCompte = :id")
    Optional<CompteUtilisateur> findByIdWithPersonne(@Param("id") Long id);
    
    boolean existsByLogin(String login);
    
    @Query("SELECT COUNT(c) FROM CompteUtilisateur c WHERE c.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT c FROM CompteUtilisateur c WHERE c.personne.id = :personneId")
    List<CompteUtilisateur> findByPersonneId(@Param("personneId") Long personneId);
    
    @Query("SELECT c FROM CompteUtilisateur c WHERE c.personne.id = :personneId AND c.role = :role")
    Optional<CompteUtilisateur> findByPersonneIdAndRole(@Param("personneId") Long personneId, @Param("role") Role role);
    
    @Query("SELECT c FROM CompteUtilisateur c WHERE c.login LIKE :loginPrefix% ORDER BY c.login DESC")
    List<CompteUtilisateur> findByLoginStartingWithOrderByLoginDesc(@Param("loginPrefix") String loginPrefix);
    
    List<CompteUtilisateur> findByRole(Role role);
}
