package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Note;
import com.example.gestion_de_notes.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId AND n.anneeUniversitaire = :annee")
    List<Note> findByEtudiantAndAnnee(@Param("etudiantId") Long etudiantId, @Param("annee") String annee);
    
    @Query("SELECT n FROM Note n WHERE n.element.id = :elementId AND n.session = :session AND n.anneeUniversitaire = :annee")
    List<Note> findByElementSessionAndAnnee(@Param("elementId") Long elementId, @Param("session") Session session, @Param("annee") String annee);
    
    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId AND n.element.id = :elementId AND n.session = :session AND n.anneeUniversitaire = :annee")
    Optional<Note> findByEtudiantElementSessionAndAnnee(@Param("etudiantId") Long etudiantId, @Param("elementId") Long elementId, @Param("session") Session session, @Param("annee") String annee);
    
    @Query("SELECT n FROM Note n WHERE n.etudiant.cne = :cne AND n.anneeUniversitaire = :annee")
    List<Note> findByEtudiantCneAndAnnee(@Param("cne") String cne, @Param("annee") String annee);
    
    @Query("SELECT n FROM Note n WHERE n.element.module.id = :moduleId AND n.session = :session AND n.anneeUniversitaire = :annee")
    List<Note> findByModuleSessionAndAnnee(@Param("moduleId") Long moduleId, @Param("session") Session session, @Param("annee") String annee);
}
