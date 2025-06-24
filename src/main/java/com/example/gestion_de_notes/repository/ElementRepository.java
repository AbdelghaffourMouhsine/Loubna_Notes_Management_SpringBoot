package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Element;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElementRepository extends JpaRepository<Element, Long> {
    
    Optional<Element> findByCode(String code);
    
    List<Element> findByTitreContainingIgnoreCase(String titre);
    
    @Query("SELECT e FROM Element e WHERE e.module.id = :moduleId")
    List<Element> findByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT e FROM Element e WHERE e.module.code = :moduleCode")
    List<Element> findByModuleCode(@Param("moduleCode") String moduleCode);
    
    boolean existsByCode(String code);
    
    @Query("SELECT COUNT(e) FROM Element e WHERE e.module.id = :moduleId")
    long countByModuleId(@Param("moduleId") Long moduleId);
}
