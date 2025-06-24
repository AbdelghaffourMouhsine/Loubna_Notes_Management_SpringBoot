package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.ElementDTO;
import com.example.gestion_de_notes.entity.Element;
import com.example.gestion_de_notes.entity.Module;
import com.example.gestion_de_notes.repository.ElementRepository;
import com.example.gestion_de_notes.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ElementService {

    @Autowired
    private ElementRepository elementRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    public List<ElementDTO> findAll() {
        return elementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ElementDTO> findById(Long id) {
        return elementRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<ElementDTO> findByModuleId(Long moduleId) {
        return elementRepository.findByModuleIdModule(moduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ElementDTO save(ElementDTO elementDTO) {
        Element element = convertToEntity(elementDTO);
        Element savedElement = elementRepository.save(element);
        return convertToDTO(savedElement);
    }

    public void deleteById(Long id) {
        elementRepository.deleteById(id);
    }

    public boolean existsByCodeAndModule(String code, Long moduleId) {
        return elementRepository.existsByCodeAndModuleIdModule(code, moduleId);
    }

    public boolean existsByCodeAndModuleAndIdNot(String code, Long moduleId, Long id) {
        return elementRepository.existsByCodeAndModuleIdModuleAndIdElementNot(code, moduleId, id);
    }

    private ElementDTO convertToDTO(Element element) {
        ElementDTO dto = new ElementDTO();
        dto.setIdElement(element.getIdElement());
        dto.setTitre(element.getTitre());
        dto.setCode(element.getCode());
        dto.setIdModule(element.getModule().getIdModule());
        dto.setModuleTitre(element.getModule().getTitre());
        dto.setModuleCode(element.getModule().getCode());
        return dto;
    }

    private Element convertToEntity(ElementDTO dto) {
        Element element = new Element();
        element.setIdElement(dto.getIdElement());
        element.setTitre(dto.getTitre());
        element.setCode(dto.getCode());
        
        if (dto.getIdModule() != null) {
            Optional<Module> module = moduleRepository.findById(dto.getIdModule());
            module.ifPresent(element::setModule);
        }
        
        return element;
    }
}
