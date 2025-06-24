package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long>, JpaSpecificationExecutor<Etudiant> {
    
    Optional<Etudiant> findByCne(String cne);
    
    List<Etudiant> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
    
    @Query("SELECT e FROM Etudiant e WHERE CONCAT(e.nom, ' ', e.prenom) LIKE %:nomComplet%")
    List<Etudiant> findByNomCompletContaining(@Param("nomComplet") String nomComplet);
    
    @Query("SELECT e FROM Etudiant e JOIN e.inscriptions i WHERE i.niveau.id = :niveauId AND i.anneeUniversitaire = :annee")
    List<Etudiant> findByNiveauAndAnnee(@Param("niveauId") Long niveauId, @Param("annee") String annee);
    
    @Query("SELECT e FROM Etudiant e JOIN e.inscriptions i WHERE i.niveau.alias = :niveauAlias AND i.anneeUniversitaire = :annee")
    List<Etudiant> findByNiveauAliasAndAnnee(@Param("niveauAlias") String niveauAlias, @Param("annee") String annee);
    
    boolean existsByCne(String cne);
    
    @Query("SELECT COUNT(e) FROM Etudiant e JOIN e.inscriptions i WHERE i.niveau.id = :niveauId AND i.anneeUniversitaire = :annee")
    long countByNiveauAndAnnee(@Param("niveauId") Long niveauId, @Param("annee") String annee);
    
    // Nouvelles méthodes pour la recherche avancée
    @Query("SELECT DISTINCT e FROM Etudiant e JOIN e.inscriptions i WHERE " +
           "(:cne IS NULL OR LOWER(e.cne) LIKE LOWER(CONCAT('%', :cne, '%'))) AND " +
           "(:nom IS NULL OR LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
           "(:prenom IS NULL OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :prenom, '%'))) AND " +
           "(:niveauId IS NULL OR i.niveau.id = :niveauId) AND " +
           "(:anneeUniversitaire IS NULL OR i.anneeUniversitaire = :anneeUniversitaire)")
    List<Etudiant> findByMultipleCriteria(@Param("cne") String cne,
                                         @Param("nom") String nom,
                                         @Param("prenom") String prenom,
                                         @Param("niveauId") Long niveauId,
                                         @Param("anneeUniversitaire") String anneeUniversitaire);
    
    @Query("SELECT e FROM Etudiant e WHERE e.id IN :ids")
    List<Etudiant> findByIdIn(@Param("ids") List<Long> ids);
    
    @Query("SELECT DISTINCT i.anneeUniversitaire FROM Inscription i ORDER BY i.anneeUniversitaire DESC")
    List<String> findDistinctAnneesUniversitaires();
}
