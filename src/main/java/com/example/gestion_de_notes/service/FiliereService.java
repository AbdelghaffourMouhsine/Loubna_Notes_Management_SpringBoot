package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.FiliereDTO;
import com.example.gestion_de_notes.entity.Filiere;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.repository.FiliereRepository;
import com.example.gestion_de_notes.repository.PersonneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FiliereService {
    
    @Autowired
    private FiliereRepository filiereRepository;
    
    @Autowired
    private PersonneRepository personneRepository;
    
    public List<FiliereDTO> findAll() {
        return filiereRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<FiliereDTO> findById(Long id) {
        return filiereRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<FiliereDTO> findByAlias(String alias) {
        return filiereRepository.findByAlias(alias)
                .map(this::convertToDTO);
    }
    
    public FiliereDTO save(FiliereDTO filiereDTO) {
        Filiere filiere = convertToEntity(filiereDTO);
        Filiere savedFiliere = filiereRepository.save(filiere);
        return convertToDTO(savedFiliere);
    }
    
    public void deleteById(Long id) {
        filiereRepository.deleteById(id);
    }
    
    public boolean existsByAlias(String alias) {
        return filiereRepository.existsByAlias(alias);
    }
    
    public List<FiliereDTO> findByCoordonnateur(Long coordonnateurId) {
        return filiereRepository.findByCoordonnateur(coordonnateurId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private FiliereDTO convertToDTO(Filiere filiere) {
        FiliereDTO dto = new FiliereDTO();
        dto.setIdFiliere(filiere.getIdFiliere());
        dto.setAlias(filiere.getAlias());
        dto.setIntitule(filiere.getIntitule());
        dto.setAnneeAccreditation(filiere.getAnneeAccreditation());
        dto.setAnneeFinAccreditation(filiere.getAnneeFinAccreditation());
        if (filiere.getCoordonnateur() != null) {
            dto.setIdCoordonnateur(filiere.getCoordonnateur().getIdPersonne());
            dto.setNomCoordonnateur(filiere.getCoordonnateur().getNomComplet());
        }
        return dto;
    }
    
    private Filiere convertToEntity(FiliereDTO dto) {
        Filiere filiere = new Filiere();
        filiere.setIdFiliere(dto.getIdFiliere());
        filiere.setAlias(dto.getAlias());
        filiere.setIntitule(dto.getIntitule());
        filiere.setAnneeAccreditation(dto.getAnneeAccreditation());
        filiere.setAnneeFinAccreditation(dto.getAnneeFinAccreditation());
        
        if (dto.getIdCoordonnateur() != null) {
            Optional<Personne> coordonnateur = personneRepository.findById(dto.getIdCoordonnateur());
            coordonnateur.ifPresent(filiere::setCoordonnateur);
        }
        
        return filiere;
    }
}
