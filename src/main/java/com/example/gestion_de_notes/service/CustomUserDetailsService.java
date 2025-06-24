package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.repository.CompteUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final CompteUtilisateurRepository compteRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CompteUtilisateur compte = compteRepository.findByLoginWithPersonne(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
        
        return compte;
    }
}
