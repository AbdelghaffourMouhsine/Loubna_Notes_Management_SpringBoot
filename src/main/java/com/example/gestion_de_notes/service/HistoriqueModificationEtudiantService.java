package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.Etudiant;
import com.example.gestion_de_notes.entity.HistoriqueModificationEtudiant;
import com.example.gestion_de_notes.repository.HistoriqueModificationEtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistoriqueModificationEtudiantService {
    
    @Autowired
    private HistoriqueModificationEtudiantRepository historiqueRepository;
    
    public void enregistrerModification(Etudiant etudiant, 
                                      String ancienCne, String ancienNom, String ancienPrenom,
                                      String nouveauCne, String nouveauNom, String nouveauPrenom,
                                      CompteUtilisateur utilisateur) {
        
        HistoriqueModificationEtudiant historique = new HistoriqueModificationEtudiant();
        historique.setEtudiant(etudiant);
        historique.setAncienCne(ancienCne);
        historique.setAncienNom(ancienNom);
        historique.setAncienPrenom(ancienPrenom);
        historique.setNouveauCne(nouveauCne);
        historique.setNouveauNom(nouveauNom);
        historique.setNouveauPrenom(nouveauPrenom);
        historique.setUtilisateurModification(utilisateur);
        
        historiqueRepository.save(historique);
    }
    
    public List<HistoriqueModificationEtudiant> findByEtudiant(Long etudiantId) {
        return historiqueRepository.findByEtudiantIdOrderByDateDesc(etudiantId);
    }
    
    public List<HistoriqueModificationEtudiant> findByUtilisateur(Long utilisateurId) {
        return historiqueRepository.findByUtilisateurIdOrderByDateDesc(utilisateurId);
    }
}
