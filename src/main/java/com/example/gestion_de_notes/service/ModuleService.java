package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.ModuleDTO;
import com.example.gestion_de_notes.entity.Module;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.repository.ModuleRepository;
import com.example.gestion_de_notes.repository.NiveauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private NiveauRepository niveauRepository;

    public List<ModuleDTO> findAll() {
        return moduleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ModuleDTO> findById(Long id) {
        return moduleRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<ModuleDTO> findByNiveauId(Long niveauId) {
        return moduleRepository.findByNiveauIdNiveau(niveauId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ModuleDTO save(ModuleDTO moduleDTO) {
        Module module = convertToEntity(moduleDTO);
        Module savedModule = moduleRepository.save(module);
        return convertToDTO(savedModule);
    }

    public void deleteById(Long id) {
        moduleRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return moduleRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return moduleRepository.existsByCodeAndIdModuleNot(code, id);
    }

    private ModuleDTO convertToDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setIdModule(module.getIdModule());
        dto.setTitre(module.getTitre());
        dto.setCode(module.getCode());
        dto.setIdNiveau(module.getNiveau().getIdNiveau());
        dto.setNiveauLibelle(module.getNiveau().getLibelle());
        dto.setNiveauAlias(module.getNiveau().getAlias());
        return dto;
    }

    private Module convertToEntity(ModuleDTO dto) {
        Module module = new Module();
        module.setIdModule(dto.getIdModule());
        module.setTitre(dto.getTitre());
        module.setCode(dto.getCode());
        
        if (dto.getIdNiveau() != null) {
            Optional<Niveau> niveau = niveauRepository.findById(dto.getIdNiveau());
            niveau.ifPresent(module::setNiveau);
        }
        
        return module;
    }
}
