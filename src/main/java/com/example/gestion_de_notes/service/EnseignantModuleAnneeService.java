package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.EnseignantModuleAnneeDTO;
import com.example.gestion_de_notes.entity.EnseignantModuleAnnee;
import com.example.gestion_de_notes.entity.Module;
import com.example.gestion_de_notes.entity.Element;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.repository.EnseignantModuleAnneeRepository;
import com.example.gestion_de_notes.repository.ModuleRepository;
import com.example.gestion_de_notes.repository.ElementRepository;
import com.example.gestion_de_notes.repository.PersonneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnseignantModuleAnneeService {

    @Autowired
    private EnseignantModuleAnneeRepository enseignantModuleAnneeRepository;

    @Autowired
    private PersonneRepository personneRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ElementRepository elementRepository;

    public List<EnseignantModuleAnneeDTO> findAll() {
        return enseignantModuleAnneeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<EnseignantModuleAnneeDTO> findById(Long id) {
        return enseignantModuleAnneeRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<EnseignantModuleAnneeDTO> findByAnneeUniversitaire(String anneeUniversitaire) {
        return enseignantModuleAnneeRepository.findByAnneeUniversitaire(anneeUniversitaire).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnseignantModuleAnneeDTO> findByEnseignantId(Long enseignantId) {
        return enseignantModuleAnneeRepository.findByEnseignantIdPersonne(enseignantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnseignantModuleAnneeDTO> findByModuleId(Long moduleId) {
        return enseignantModuleAnneeRepository.findByModuleIdModule(moduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EnseignantModuleAnneeDTO save(EnseignantModuleAnneeDTO dto) {
        EnseignantModuleAnnee entity = convertToEntity(dto);
        EnseignantModuleAnnee saved = enseignantModuleAnneeRepository.save(entity);
        return convertToDTO(saved);
    }

    public void deleteById(Long id) {
        enseignantModuleAnneeRepository.deleteById(id);
    }

    public boolean existsByModuleAndAnnee(Long moduleId, String anneeUniversitaire) {
        return enseignantModuleAnneeRepository.existsByModuleIdModuleAndAnneeUniversitaire(moduleId, anneeUniversitaire);
    }

    public boolean existsByElementAndAnnee(Long elementId, String anneeUniversitaire) {
        return enseignantModuleAnneeRepository.existsByElementIdElementAndAnneeUniversitaire(elementId, anneeUniversitaire);
    }

    private EnseignantModuleAnneeDTO convertToDTO(EnseignantModuleAnnee entity) {
        EnseignantModuleAnneeDTO dto = new EnseignantModuleAnneeDTO();
        dto.setId(entity.getId());
        dto.setAnneeUniversitaire(entity.getAnneeUniversitaire());
        
        if (entity.getEnseignant() != null) {
            dto.setIdEnseignant(entity.getEnseignant().getIdPersonne());
            dto.setEnseignantNom(entity.getEnseignant().getNom());
            dto.setEnseignantPrenom(entity.getEnseignant().getPrenom());
            dto.setEnseignantEmail(entity.getEnseignant().getEmail());
        }
        
        if (entity.getModule() != null) {
            dto.setIdModule(entity.getModule().getIdModule());
            dto.setModuleTitre(entity.getModule().getTitre());
            dto.setModuleCode(entity.getModule().getCode());
        }
        
        if (entity.getElement() != null) {
            dto.setIdElement(entity.getElement().getIdElement());
            dto.setElementTitre(entity.getElement().getTitre());
            dto.setElementCode(entity.getElement().getCode());
        }
        
        return dto;
    }

    private EnseignantModuleAnnee convertToEntity(EnseignantModuleAnneeDTO dto) {
        EnseignantModuleAnnee entity = new EnseignantModuleAnnee();
        entity.setId(dto.getId());
        entity.setAnneeUniversitaire(dto.getAnneeUniversitaire());
        
        if (dto.getIdEnseignant() != null) {
            Optional<Personne> enseignant = personneRepository.findById(dto.getIdEnseignant());
            enseignant.ifPresent(entity::setEnseignant);
        }
        
        if (dto.getIdModule() != null) {
            Optional<Module> module = moduleRepository.findById(dto.getIdModule());
            module.ifPresent(entity::setModule);
        }
        
        if (dto.getIdElement() != null) {
            Optional<Element> element = elementRepository.findById(dto.getIdElement());
            element.ifPresent(entity::setElement);
        }
        
        return entity;
    }
}
