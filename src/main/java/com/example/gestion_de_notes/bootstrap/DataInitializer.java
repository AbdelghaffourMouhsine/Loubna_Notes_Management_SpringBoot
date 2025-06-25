package com.example.gestion_de_notes.bootstrap;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.entity.Role;
import com.example.gestion_de_notes.repository.CompteUtilisateurRepository;
import com.example.gestion_de_notes.repository.PersonneRepository;
import com.example.gestion_de_notes.service.DataInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final PersonneRepository personneRepository;
    private final CompteUtilisateurRepository compteRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataInitializationService dataInitializationService;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdminUser();
        initializeNiveaux();
    }
    
    private void initializeDefaultAdminUser() {
        // Vérifier si un compte ADMIN_USER existe déjà
        if (compteRepository.countByRole(Role.ADMIN_USER) > 0) {
            log.info("Un compte ADMIN_USER existe déjà, pas d'initialisation nécessaire");
            return;
        }        
        log.info("Initialisation du compte ADMIN_USER par défaut...");
        
        // Créer la personne par défaut pour l'admin
        Personne adminPersonne = new Personne();
        adminPersonne.setNom("Admin");
        adminPersonne.setPrenom("System");
        adminPersonne.setEmail("admin@gestion-notes.com");
        adminPersonne.setCin("ADMIN001");
        adminPersonne.setTelephone("0000000000");
        
        adminPersonne = personneRepository.save(adminPersonne);
        log.info("Personne admin créée: {}", adminPersonne.getNomComplet());
        
        // Créer le compte ADMIN_USER
        CompteUtilisateur adminCompte = new CompteUtilisateur();
        adminCompte.setLogin("admin");
        adminCompte.setMotDePasse(passwordEncoder.encode("admin123"));
        adminCompte.setRole(Role.ADMIN_USER);
        adminCompte.setPersonne(adminPersonne);
        adminCompte.setEnabled(true);
        adminCompte.setLocked(false);
        
        adminCompte = compteRepository.save(adminCompte);
        log.info("Compte ADMIN_USER créé avec le login: {}", adminCompte.getLogin());
        log.info("Mot de passe par défaut: admin123");
        log.info("IMPORTANT: Changez ce mot de passe après la première connexion!");
    }
    
    private void initializeNiveaux() {
        log.info("Initialisation des niveaux...");
        dataInitializationService.initialiserNiveaux();
        log.info("Niveaux initialisés avec succès");
    }
}
