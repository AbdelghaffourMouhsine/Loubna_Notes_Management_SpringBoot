package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.PersonneDTO;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.repository.PersonneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonneService {
    
    private final PersonneRepository personneRepository;
    
    public List<Personne> findAll() {
        return personneRepository.findAll();
    }
    
    public Optional<Personne> findById(Long id) {
        return personneRepository.findById(id);
    }
    
    public Optional<Personne> findByCin(String cin) {
        return personneRepository.findByCin(cin);
    }
    
    public List<Personne> searchPersonnes(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return personneRepository.findByNomOrPrenomOrCinContainingIgnoreCase(searchTerm.trim());
    }
    
    public List<Personne> findByNomAndCin(String nom, String cin) {
        return personneRepository.findByNomAndCin(nom, cin);
    }
    
    public Personne save(PersonneDTO personneDTO) {
        if (personneDTO.getIdPersonne() == null && personneRepository.existsByCin(personneDTO.getCin())) {
            throw new IllegalArgumentException("Une personne avec ce CIN existe déjà");
        }
        
        Personne personne = convertToEntity(personneDTO);
        return personneRepository.save(personne);
    }
    
    public Personne update(Long id, PersonneDTO personneDTO) {
        Personne existingPersonne = personneRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée avec l'ID: " + id));
        
        // Vérifier que le CIN n'est pas déjà utilisé par une autre personne
        if (!existingPersonne.getCin().equals(personneDTO.getCin()) && 
            personneRepository.existsByCin(personneDTO.getCin())) {
            throw new IllegalArgumentException("Une autre personne avec ce CIN existe déjà");
        }
        
        existingPersonne.setNom(personneDTO.getNom());
        existingPersonne.setPrenom(personneDTO.getPrenom());
        existingPersonne.setEmail(personneDTO.getEmail());
        existingPersonne.setTelephone(personneDTO.getTelephone());
        existingPersonne.setCin(personneDTO.getCin());
        
        return personneRepository.save(existingPersonne);
    }
    
    public void deleteById(Long id) {
        if (!personneRepository.existsById(id)) {
            throw new IllegalArgumentException("Personne non trouvée avec l'ID: " + id);
        }
        personneRepository.deleteById(id);
    }
    
    public PersonneDTO convertToDTO(Personne personne) {
        if (personne == null) return null;
        
        PersonneDTO dto = new PersonneDTO();
        dto.setIdPersonne(personne.getIdPersonne());
        dto.setNom(personne.getNom());
        dto.setPrenom(personne.getPrenom());
        dto.setEmail(personne.getEmail());
        dto.setTelephone(personne.getTelephone());
        dto.setCin(personne.getCin());
        return dto;
    }
    
    private Personne convertToEntity(PersonneDTO dto) {
        if (dto == null) return null;
        
        Personne personne = new Personne();
        personne.setIdPersonne(dto.getIdPersonne());
        personne.setNom(dto.getNom());
        personne.setPrenom(dto.getPrenom());
        personne.setEmail(dto.getEmail());
        personne.setTelephone(dto.getTelephone());
        personne.setCin(dto.getCin());
        return personne;
    }
}
