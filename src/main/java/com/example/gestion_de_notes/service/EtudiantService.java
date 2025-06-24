package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.EtudiantDTO;
import com.example.gestion_de_notes.entity.Etudiant;
import com.example.gestion_de_notes.repository.EtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EtudiantService {
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    public List<EtudiantDTO> findAll() {
        return etudiantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<EtudiantDTO> findById(Long id) {
        return etudiantRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<EtudiantDTO> findByCne(String cne) {
        return etudiantRepository.findByCne(cne)
                .map(this::convertToDTO);
    }
    
    public EtudiantDTO save(EtudiantDTO etudiantDTO) {
        Etudiant etudiant = convertToEntity(etudiantDTO);
        Etudiant savedEtudiant = etudiantRepository.save(etudiant);
        return convertToDTO(savedEtudiant);
    }
    
    public void deleteById(Long id) {
        etudiantRepository.deleteById(id);
    }
    
    public boolean existsByCne(String cne) {
        return etudiantRepository.existsByCne(cne);
    }
    
    public List<EtudiantDTO> findByNomCompletContaining(String nomComplet) {
        return etudiantRepository.findByNomCompletContaining(nomComplet).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<EtudiantDTO> findByNiveauAndAnnee(Long niveauId, String annee) {
        return etudiantRepository.findByNiveauAndAnnee(niveauId, annee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private EtudiantDTO convertToDTO(Etudiant etudiant) {
        EtudiantDTO dto = new EtudiantDTO();
        dto.setIdEtudiant(etudiant.getIdEtudiant());
        dto.setCne(etudiant.getCne());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setNomComplet(etudiant.getNomComplet());
        return dto;
    }
    
    private Etudiant convertToEntity(EtudiantDTO dto) {
        Etudiant etudiant = new Etudiant();
        etudiant.setIdEtudiant(dto.getIdEtudiant());
        etudiant.setCne(dto.getCne());
        etudiant.setNom(dto.getNom());
        etudiant.setPrenom(dto.getPrenom());
        return etudiant;
    }
}
