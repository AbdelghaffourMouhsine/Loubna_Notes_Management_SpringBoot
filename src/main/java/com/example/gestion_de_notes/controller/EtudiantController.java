package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.*;
import com.example.gestion_de_notes.entity.HistoriqueModificationEtudiant;
import com.example.gestion_de_notes.entity.Niveau;
import com.example.gestion_de_notes.service.EtudiantService;
import com.example.gestion_de_notes.service.EtudiantExportService;
import com.example.gestion_de_notes.service.NiveauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/notes/etudiants")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class EtudiantController {
    
    @Autowired
    private EtudiantService etudiantService;
    
    @Autowired
    private NiveauService niveauService;
    
    @Autowired
    private EtudiantExportService exportService;
    
    /**
     * Méthode utilitaire pour ajouter les niveaux au modèle de façon sécurisée
     */
    private void addNiveauxToModel(Model model) {
        try {
            model.addAttribute("niveaux", niveauService.findAll());
        } catch (Exception e) {
            // En cas d'erreur, on met une liste vide
            model.addAttribute("niveaux", java.util.Collections.emptyList());
        }
    }
    
    @GetMapping
    public String listEtudiants(Model model) {
        try {
            List<EtudiantDTO> etudiants = etudiantService.findAll();
            model.addAttribute("etudiants", etudiants);
            
            // Ajouter des informations utiles au modèle
            model.addAttribute("totalEtudiants", etudiants != null ? etudiants.size() : 0);
            
        } catch (Exception e) {
            // Log de l'erreur pour le débogage
            System.err.println("Erreur lors du chargement des étudiants: " + e.getMessage());
            e.printStackTrace();
            
            // Ajouter un message d'erreur au modèle
            model.addAttribute("error", "Erreur lors du chargement des étudiants: " + e.getMessage());
            
            // Initialiser une liste vide pour éviter les erreurs dans le template
            model.addAttribute("etudiants", java.util.Collections.emptyList());
        }
        
        return "admin/notes/etudiants/list";
    }
    
    @GetMapping("/search")
    public String searchEtudiants(@ModelAttribute EtudiantSearchDTO searchDTO,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EtudiantDTO> etudiants = etudiantService.searchEtudiants(searchDTO, pageable);
        
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("niveaux", niveauService.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", etudiants.getTotalPages());
        model.addAttribute("searching", true);
        
        return "admin/notes/etudiants/list";
    }
    
    @GetMapping("/classe/{niveauId}")
    public String getEtudiantsByClasse(@PathVariable Long niveauId,
                                      @RequestParam String anneeUniversitaire,
                                      Model model) {
        List<EtudiantDetailDTO> etudiants = etudiantService.getEtudiantsByClasse(niveauId, anneeUniversitaire);
        Optional<NiveauDTO> niveau = niveauService.findById(niveauId);
        
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("niveau", niveau.orElse(null));
        model.addAttribute("anneeUniversitaire", anneeUniversitaire);
        
        return "admin/notes/etudiants/classe";
    }
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportEtudiants(@ModelAttribute EtudiantSearchDTO searchDTO) {
        try {
            byte[] csvData = exportService.exportEtudiantsToCSV(searchDTO);
            String filename = exportService.generateFileName("etudiants");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/export-selection")
    public ResponseEntity<byte[]> exportSelectedEtudiants(@RequestParam List<Long> etudiantIds) {
        try {
            byte[] csvData = exportService.exportSelectedEtudiantsToCSV(etudiantIds);
            String filename = exportService.generateFileName("etudiants_selection");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/export-classe")
    public ResponseEntity<byte[]> exportClasse(@RequestParam Long niveauId, 
                                              @RequestParam String anneeUniversitaire) {
        try {
            byte[] csvData = exportService.exportClasseToCSV(niveauId, anneeUniversitaire);
            String filename = exportService.generateFileName("classe_" + niveauId);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Pour le test initial, on n'ajoute que l'objet étudiant
        model.addAttribute("etudiantDTO", new EtudiantDTO());
        return "admin/notes/etudiants/form";
    }
    
    @PostMapping
    public String createEtudiant(@RequestParam String cne,
                                @RequestParam String nom,
                                @RequestParam String prenom,
                                RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que le CNE n'existe pas déjà
            if (etudiantService.existsByCne(cne)) {
                redirectAttributes.addFlashAttribute("error", "Un étudiant avec ce CNE existe déjà");
                return "redirect:/admin/notes/etudiants/new";
            }
            
            // Créer l'étudiant
            EtudiantDTO etudiantDTO = new EtudiantDTO();
            etudiantDTO.setCne(cne);
            etudiantDTO.setNom(nom);
            etudiantDTO.setPrenom(prenom);
            
            etudiantService.save(etudiantDTO);
            redirectAttributes.addFlashAttribute("success", "Étudiant créé avec succès");
            return "redirect:/admin/notes/etudiants";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/admin/notes/etudiants/new";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantDTO> etudiant = etudiantService.findById(id);
        if (etudiant.isPresent()) {
            model.addAttribute("etudiantDTO", etudiant.get());
            addNiveauxToModel(model);
            return "admin/notes/etudiants/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé");
            return "redirect:/admin/notes/etudiants";
        }
    }
    
    @PostMapping("/{id}")
    public String updateEtudiant(@PathVariable Long id, 
                                @Valid @ModelAttribute EtudiantDTO etudiantDTO,
                                BindingResult result, 
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("niveaux", niveauService.findAll());
            return "admin/notes/etudiants/form";
        }
        
        etudiantDTO.setIdEtudiant(id);
        
        try {
            etudiantService.updateEtudiant(etudiantDTO);
            redirectAttributes.addFlashAttribute("success", "Étudiant modifié avec succès");
            return "redirect:/admin/notes/etudiants";
        } catch (Exception e) {
            result.reject("error.general", "Erreur lors de la modification: " + e.getMessage());
            model.addAttribute("niveaux", niveauService.findAll());
            return "admin/notes/etudiants/form";
        }
    }
    
    @DeleteMapping("/{id}")
    public String deleteEtudiant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            etudiantService.deleteEtudiantAvecControles(id);
            redirectAttributes.addFlashAttribute("success", "Étudiant supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer l'étudiant: " + e.getMessage());
        }
        return "redirect:/admin/notes/etudiants";
    }
    
    @GetMapping("/{id}")
    public String viewEtudiant(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantCompletDTO> etudiantComplet = etudiantService.getEtudiantComplet(id);
        if (etudiantComplet.isPresent()) {
            List<HistoriqueModificationEtudiant> historique = etudiantService.getHistoriqueModifications(id);
            
            model.addAttribute("etudiantComplet", etudiantComplet.get());
            model.addAttribute("historique", historique);
            return "admin/notes/etudiants/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé");
            return "redirect:/admin/notes/etudiants";
        }
    }
    
    @GetMapping("/{id}/historique")
    public String viewHistorique(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantDTO> etudiant = etudiantService.findById(id);
        if (!etudiant.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé");
            return "redirect:/admin/notes/etudiants";
        }
        
        List<HistoriqueModificationEtudiant> historique = etudiantService.getHistoriqueModifications(id);
        
        model.addAttribute("etudiant", etudiant.get());
        model.addAttribute("historique", historique);
        
        return "admin/notes/etudiants/historique";
    }
    
    @GetMapping("/{id}/modifier-niveau")
    public String showModifierNiveauForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantDTO> etudiant = etudiantService.findById(id);
        if (!etudiant.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé");
            return "redirect:/admin/notes/etudiants";
        }
        
        model.addAttribute("etudiant", etudiant.get());
        model.addAttribute("niveaux", niveauService.findAll());
        
        return "admin/notes/etudiants/modifier-niveau";
    }
    
    @PostMapping("/{id}/modifier-niveau")
    public String modifierNiveauEtudiant(@PathVariable Long id,
                                        @RequestParam Long nouveauNiveauId,
                                        @RequestParam String anneeUniversitaire,
                                        RedirectAttributes redirectAttributes) {
        try {
            etudiantService.modifierNiveauEtudiant(id, nouveauNiveauId, anneeUniversitaire);
            redirectAttributes.addFlashAttribute("success", "Niveau de l'étudiant modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification du niveau: " + e.getMessage());
        }
        
        return "redirect:/admin/notes/etudiants/" + id;
    }
}
