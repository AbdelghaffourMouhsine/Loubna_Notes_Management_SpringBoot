package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.NiveauDTO;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.repository.NiveauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NiveauService {
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    public List<NiveauDTO> findAll() {
        return niveauRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<NiveauDTO> findById(Long id) {
        return niveauRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<NiveauDTO> findByAlias(String alias) {
        return niveauRepository.findByAlias(alias)
                .map(this::convertToDTO);
    }
    
    public NiveauDTO save(NiveauDTO niveauDTO) {
        Niveau niveau = convertToEntity(niveauDTO);
        Niveau savedNiveau = niveauRepository.save(niveau);
        return convertToDTO(savedNiveau);
    }
    
    public void deleteById(Long id) {
        niveauRepository.deleteById(id);
    }
    
    public boolean existsByAlias(String alias) {
        return niveauRepository.existsByAlias(alias);
    }
    
    public boolean existsByIdNiveau(Long idNiveau) {
        return niveauRepository.existsByIdNiveau(idNiveau);
    }
    
    public List<NiveauDTO> findByFiliereId(Long filiereId) {
        return niveauRepository.findByFiliereId(filiereId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private NiveauDTO convertToDTO(Niveau niveau) {
        NiveauDTO dto = new NiveauDTO();
        dto.setIdNiveau(niveau.getIdNiveau());
        dto.setLibelle(niveau.getLibelle());
        dto.setAlias(niveau.getAlias());
        if (niveau.getNiveauSuivant() != null) {
            dto.setIdNiveauSuivant(niveau.getNiveauSuivant().getIdNiveau());
            dto.setAliasNiveauSuivant(niveau.getNiveauSuivant().getAlias());
        }
        return dto;
    }
    
    private Niveau convertToEntity(NiveauDTO dto) {
        Niveau niveau = new Niveau();
        niveau.setIdNiveau(dto.getIdNiveau());
        niveau.setLibelle(dto.getLibelle());
        niveau.setAlias(dto.getAlias());
        
        if (dto.getIdNiveauSuivant() != null) {
            Optional<Niveau> niveauSuivant = niveauRepository.findById(dto.getIdNiveauSuivant());
            niveauSuivant.ifPresent(niveau::setNiveauSuivant);
        }
        
        return niveau;
    }
}
