package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.FiliereDTO;
import com.example.gestion_de_notes.dto.NiveauDTO;
import com.example.gestion_de_notes.entity.Filiere;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.repository.FiliereRepository;
import com.example.gestion_de_notes.repository.NiveauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NiveauService {
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    @Autowired
    private FiliereRepository filiereRepository;
    
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
        
        // Gestion des filières associées
        if (niveauDTO.getIdsFilieres() != null && !niveauDTO.getIdsFilieres().isEmpty()) {
            List<Filiere> filieres = new ArrayList<>();
            for (Long idFiliere : niveauDTO.getIdsFilieres()) {
                Optional<Filiere> filiere = filiereRepository.findById(idFiliere);
                filiere.ifPresent(filieres::add);
            }
            niveau.setFilieres(filieres);
        } else {
            niveau.setFilieres(new ArrayList<>());
        }
        
        Niveau savedNiveau = niveauRepository.save(niveau);
        return convertToDTO(savedNiveau);
    }
    
    public void deleteById(Long id) {
        niveauRepository.deleteById(id);
    }
    
    public boolean existsByAlias(String alias) {
        return niveauRepository.existsByAlias(alias);
    }
    
    public boolean existsByAliasAndIdNot(String alias, Long id) {
        return niveauRepository.existsByAliasAndIdNiveauNot(alias, id);
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
            dto.setNiveauSuivantAlias(niveau.getNiveauSuivant().getAlias());
        }
        
        // Gestion des filières associées
        if (niveau.getFilieres() != null && !niveau.getFilieres().isEmpty()) {
            List<Long> idsFilieres = niveau.getFilieres().stream()
                    .map(Filiere::getIdFiliere)
                    .collect(Collectors.toList());
            dto.setIdsFilieres(idsFilieres);
            
            List<FiliereDTO> filieresDTO = niveau.getFilieres().stream()
                    .map(this::convertFiliereToDTO)
                    .collect(Collectors.toList());
            dto.setFilieres(filieresDTO);
        } else {
            dto.setIdsFilieres(new ArrayList<>());
            dto.setFilieres(new ArrayList<>());
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
    
    private FiliereDTO convertFiliereToDTO(Filiere filiere) {
        FiliereDTO dto = new FiliereDTO();
        dto.setIdFiliere(filiere.getIdFiliere());
        dto.setAlias(filiere.getAlias());
        dto.setIntitule(filiere.getIntitule());
        dto.setAnneeAccreditation(filiere.getAnneeAccreditation());
        dto.setAnneeFinAccreditation(filiere.getAnneeFinAccreditation());
        if (filiere.getCoordonnateur() != null) {
            dto.setIdCoordonnateur(filiere.getCoordonnateur().getIdPersonne());
            dto.setNomCoordonnateur(filiere.getCoordonnateur().getNom() + " " + filiere.getCoordonnateur().getPrenom());
        }
        return dto;
    }
}
