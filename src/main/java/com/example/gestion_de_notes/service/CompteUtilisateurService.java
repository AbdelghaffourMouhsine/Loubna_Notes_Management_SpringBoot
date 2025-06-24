package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.dto.CompteUtilisateurDTO;
import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.entity.Role;
import com.example.gestion_de_notes.repository.CompteUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompteUtilisateurService {
    
    private final CompteUtilisateurRepository compteRepository;
    private final PersonneService personneService;
    private final PasswordEncoder passwordEncoder;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    
    public List<CompteUtilisateur> findAll() {
        return compteRepository.findAllWithPersonnes();
    }
    
    public Optional<CompteUtilisateur> findById(Long id) {
        return compteRepository.findByIdWithPersonne(id);
    }
    
    public Optional<CompteUtilisateur> findByLogin(String login) {
        return compteRepository.findByLogin(login);
    }
    
    public List<CompteUtilisateur> findByPersonneId(Long personneId) {
        return compteRepository.findByPersonneId(personneId);
    }
    
    public CompteUtilisateurDTO createCompte(Long personneId, Role role) {
        // Vérifier qu'on ne peut pas créer plusieurs comptes ADMIN_USER
        if (role == Role.ADMIN_USER && compteRepository.countByRole(Role.ADMIN_USER) > 0) {
            throw new IllegalArgumentException("Un seul compte ADMIN_USER est autorisé");
        }
        
        // Vérifier que la personne existe
        Personne personne = personneService.findById(personneId)
            .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));
        
        // Vérifier qu'un compte avec ce rôle n'existe pas déjà pour cette personne
        if (compteRepository.findByPersonneIdAndRole(personneId, role).isPresent()) {
            throw new IllegalArgumentException("Un compte avec ce rôle existe déjà pour cette personne");
        }
        
        // Générer le login unique
        String baseLogin = generateBaseLogin(personne.getNom(), personne.getPrenom());
        String uniqueLogin = generateUniqueLogin(baseLogin);
        
        // Générer un mot de passe aléatoire
        String plainPassword = generateRandomPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        // Créer le compte
        CompteUtilisateur compte = new CompteUtilisateur();
        compte.setLogin(uniqueLogin);
        compte.setMotDePasse(hashedPassword);
        compte.setRole(role);
        compte.setPersonne(personne);
        compte.setEnabled(true);
        compte.setLocked(false);
        
        CompteUtilisateur savedCompte = compteRepository.save(compte);
        
        // Convertir en DTO et inclure le mot de passe en clair pour l'affichage
        CompteUtilisateurDTO dto = convertToDTO(savedCompte);
        dto.setMotDePasseGenere(plainPassword);
        
        return dto;
    }
    
    public CompteUtilisateur updateRole(Long compteId, Role newRole) {
        CompteUtilisateur compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé"));
        
        // Vérifier qu'on ne peut pas créer plusieurs comptes ADMIN_USER
        if (newRole == Role.ADMIN_USER && compte.getRole() != Role.ADMIN_USER && 
            compteRepository.countByRole(Role.ADMIN_USER) > 0) {
            throw new IllegalArgumentException("Un seul compte ADMIN_USER est autorisé");
        }
        
        compte.setRole(newRole);
        return compteRepository.save(compte);
    }
    
    public String resetPassword(Long compteId) {
        CompteUtilisateur compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé"));
        
        String newPassword = generateRandomPassword();
        compte.setMotDePasse(passwordEncoder.encode(newPassword));
        compteRepository.save(compte);
        
        return newPassword;
    }
    
    public CompteUtilisateur toggleEnabled(Long compteId) {
        CompteUtilisateur compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé"));
        
        compte.setEnabled(!compte.getEnabled());
        return compteRepository.save(compte);
    }
    
    public CompteUtilisateur toggleLocked(Long compteId) {
        CompteUtilisateur compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé"));
        
        compte.setLocked(!compte.getLocked());
        return compteRepository.save(compte);
    }
    
    public void updateLastAccess(String login) {
        compteRepository.findByLogin(login).ifPresent(compte -> {
            compte.setDernierAcces(LocalDateTime.now());
            compteRepository.save(compte);
        });
    }
    
    private String generateBaseLogin(String nom, String prenom) {
        String baseLogin = (nom + prenom).toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "")
            .replaceAll("\\s+", "");
        return baseLogin;
    }
    
    private String generateUniqueLogin(String baseLogin) {
        if (!compteRepository.existsByLogin(baseLogin)) {
            return baseLogin;
        }
        
        // Trouver le prochain numéro disponible
        List<CompteUtilisateur> existingAccounts = compteRepository
            .findByLoginStartingWithOrderByLoginDesc(baseLogin);
        
        int maxNumber = 0;
        for (CompteUtilisateur compte : existingAccounts) {
            String login = compte.getLogin();
            if (login.equals(baseLogin)) {
                maxNumber = Math.max(maxNumber, 0);
            } else if (login.startsWith(baseLogin)) {
                try {
                    String numberPart = login.substring(baseLogin.length());
                    int number = Integer.parseInt(numberPart);
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // Ignorer les logins qui ne se terminent pas par un nombre
                }
            }
        }
        
        return baseLogin + (maxNumber + 1);
    }
    
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return password.toString();
    }
    
    public CompteUtilisateurDTO convertToDTO(CompteUtilisateur compte) {
        if (compte == null) return null;
        
        CompteUtilisateurDTO dto = new CompteUtilisateurDTO();
        dto.setIdCompte(compte.getIdCompte());
        dto.setLogin(compte.getLogin());
        dto.setRole(compte.getRole());
        dto.setIdPersonne(compte.getPersonne().getIdPersonne());
        dto.setNomPersonne(compte.getPersonne().getNom());
        dto.setPrenomPersonne(compte.getPersonne().getPrenom());
        dto.setCinPersonne(compte.getPersonne().getCin());
        dto.setEnabled(compte.getEnabled());
        dto.setLocked(compte.getLocked());
        return dto;
    }
    
    public List<CompteUtilisateurDTO> convertToDTOList(List<CompteUtilisateur> comptes) {
        return comptes.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}
