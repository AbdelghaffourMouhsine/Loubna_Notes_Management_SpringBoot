package com.example.gestion_de_notes.repository;

import com.example.gestion_de_notes.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    Optional<Module> findByCode(String code);
    
    List<Module> findByTitreContainingIgnoreCase(String titre);
    
    List<Module> findByNiveauIdNiveau(Long niveauId);
    
    @Query("SELECT m FROM Module m WHERE m.niveau.alias = :niveauAlias")
    List<Module> findByNiveauAlias(@Param("niveauAlias") String niveauAlias);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdModuleNot(String code, Long id);
    
    @Query("SELECT COUNT(m) FROM Module m WHERE m.niveau.id = :niveauId")
    long countByNiveauId(@Param("niveauId") Long niveauId);
}
