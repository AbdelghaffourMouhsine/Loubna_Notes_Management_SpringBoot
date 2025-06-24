package com.example.gestion_de_notes.service;

import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.repository.NiveauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class DataInitializationService {
    
    @Autowired
    private NiveauRepository niveauRepository;
    
    public void initialiserNiveaux() {
        if (niveauRepository.count() == 0) {
            List<NiveauData> niveauxData = Arrays.asList(
                new NiveauData(1L, "Première Année Cycle Préparatoire", "CP1", 2L),
                new NiveauData(2L, "Deuxième Année Cycle Préparatoire", "CP2", 12L),
                new NiveauData(3L, "Génie Informatique 1", "GI1", 4L),
                new NiveauData(4L, "Génie Informatique 2", "GI2", 5L),
                new NiveauData(5L, "Génie Informatique 3", "GI3", null),
                new NiveauData(6L, "Génie Civil 1", "GC1", 7L),
                new NiveauData(7L, "Génie Civil 2", "GC2", 8L),
                new NiveauData(8L, "Génie Civil 3", "GC3", null),
                new NiveauData(9L, "Génie Environnement 1", "GE1", null),
                new NiveauData(10L, "Génie Environnement 2", "GE2", null),
                new NiveauData(11L, "Génie Environnement 3", "GE3", null),
                new NiveauData(12L, "Première Année Cycle Ingénieur", "C. Ing 1", null),
                new NiveauData(13L, "Génie Energétique et Energies renouvelables 1", "GEER1", 14L),
                new NiveauData(14L, "Génie Energétique et Energies renouvelables 2", "GEER2", 15L),
                new NiveauData(15L, "Génie Energétique et Energies renouvelables 3", "GEER3", null),
                new NiveauData(16L, "Génie de l'eau et de l'Environnement 1", "GEE1", 21L),
                new NiveauData(17L, "Génie Civil 3 Option HYD", "GC3 HYD", null),
                new NiveauData(18L, "Génie Civil 3 Option BPC", "GC3 BPC", null),
                new NiveauData(19L, "Génie Informatique 3 Option GL", "GI3 GL", null),
                new NiveauData(20L, "Génie Informatique 3 Option BI", "GI3 BI", null)
            );
            
            List<NiveauData> niveauxDataSuite = Arrays.asList(
                new NiveauData(21L, "Génie de l'eau et de l'Environnement 2", "GEE2", 22L),
                new NiveauData(22L, "Génie de l'eau et de l'Environnement 3", "GEE3", null),
                new NiveauData(23L, "Génie Mécanique 1", "GM1", 24L),
                new NiveauData(24L, "Génie Mécanique 2", "GM2", 25L),
                new NiveauData(25L, "Génie Mécanique 3", "GM3", null),
                new NiveauData(26L, "Ingénierie des données 1", "ID1", 27L),
                new NiveauData(27L, "Ingénierie des données 2", "ID2", 28L),
                new NiveauData(28L, "Ingénierie des données 3", "ID3", null),
                new NiveauData(29L, "Génie Informatique 3 Option Médias et Interactions", "GI3 MI", 25L)
            );
            
            // Première passe : créer les niveaux sans les relations
            for (NiveauData data : niveauxData) {
                Niveau niveau = new Niveau();
                niveau.setIdNiveau(data.id);
                niveau.setLibelle(data.libelle);
                niveau.setAlias(data.alias);
                niveauRepository.save(niveau);
            }
            
            for (NiveauData data : niveauxDataSuite) {
                Niveau niveau = new Niveau();
                niveau.setIdNiveau(data.id);
                niveau.setLibelle(data.libelle);
                niveau.setAlias(data.alias);
                niveauRepository.save(niveau);
            }
            
            // Deuxième passe : établir les relations
            for (NiveauData data : niveauxData) {
                if (data.niveauSuivantId != null) {
                    Niveau niveau = niveauRepository.findById(data.id).orElse(null);
                    Niveau niveauSuivant = niveauRepository.findById(data.niveauSuivantId).orElse(null);
                    if (niveau != null && niveauSuivant != null) {
                        niveau.setNiveauSuivant(niveauSuivant);
                        niveauRepository.save(niveau);
                    }
                }
            }
            
            for (NiveauData data : niveauxDataSuite) {
                if (data.niveauSuivantId != null) {
                    Niveau niveau = niveauRepository.findById(data.id).orElse(null);
                    Niveau niveauSuivant = niveauRepository.findById(data.niveauSuivantId).orElse(null);
                    if (niveau != null && niveauSuivant != null) {
                        niveau.setNiveauSuivant(niveauSuivant);
                        niveauRepository.save(niveau);
                    }
                }
            }
        }
    }
    
    private static class NiveauData {
        public Long id;
        public String libelle;
        public String alias;
        public Long niveauSuivantId;
        
        public NiveauData(Long id, String libelle, String alias, Long niveauSuivantId) {
            this.id = id;
            this.libelle = libelle;
            this.alias = alias;
            this.niveauSuivantId = niveauSuivantId;
        }
    }
}
