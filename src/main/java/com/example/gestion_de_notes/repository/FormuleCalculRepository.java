package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.FormuleCalcul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormuleCalculRepository extends JpaRepository<FormuleCalcul, Long> {
    
    Optional<FormuleCalcul> findByTypeFormule(String typeFormule);
    
    List<FormuleCalcul> findByTypeFormuleContainingIgnoreCase(String typeFormule);
}
