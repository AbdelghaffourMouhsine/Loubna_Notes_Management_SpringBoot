package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.EtudiantDTO;
import com.example.gestion_de_notes.service.EtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/notes/etudiants")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class EtudiantController {
    
    @Autowired
    private EtudiantService etudiantService;
    
    @GetMapping
    public String listEtudiants(Model model) {
        List<EtudiantDTO> etudiants = etudiantService.findAll();
        model.addAttribute("etudiants", etudiants);
        return "admin/notes/etudiants/list";
    }
    
    @GetMapping("/search")
    public String searchEtudiants(@RequestParam(required = false) String query, Model model) {
        List<EtudiantDTO> etudiants;
        if (query != null && !query.trim().isEmpty()) {
            etudiants = etudiantService.findByNomCompletContaining(query.trim());
        } else {
            etudiants = etudiantService.findAll();
        }
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("query", query);
        return "admin/notes/etudiants/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("etudiantDTO", new EtudiantDTO());
        return "admin/notes/etudiants/form";
    }
    
    @PostMapping
    public String createEtudiant(@Valid @ModelAttribute EtudiantDTO etudiantDTO, 
                                BindingResult result, 
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/notes/etudiants/form";
        }
        
        if (etudiantService.existsByCne(etudiantDTO.getCne())) {
            result.rejectValue("cne", "error.etudiant", "Un étudiant avec ce CNE existe déjà");
            return "admin/notes/etudiants/form";
        }
        
        etudiantService.save(etudiantDTO);
        redirectAttributes.addFlashAttribute("success", "Étudiant créé avec succès");
        return "redirect:/admin/notes/etudiants";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantDTO> etudiant = etudiantService.findById(id);
        if (etudiant.isPresent()) {
            model.addAttribute("etudiantDTO", etudiant.get());
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
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/notes/etudiants/form";
        }
        
        etudiantDTO.setIdEtudiant(id);
        etudiantService.save(etudiantDTO);
        redirectAttributes.addFlashAttribute("success", "Étudiant modifié avec succès");
        return "redirect:/admin/notes/etudiants";
    }
    
    @DeleteMapping("/{id}")
    public String deleteEtudiant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            etudiantService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Étudiant supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer l'étudiant");
        }
        return "redirect:/admin/notes/etudiants";
    }
    
    @GetMapping("/{id}")
    public String viewEtudiant(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtudiantDTO> etudiant = etudiantService.findById(id);
        if (etudiant.isPresent()) {
            model.addAttribute("etudiant", etudiant.get());
            return "admin/notes/etudiants/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé");
            return "redirect:/admin/notes/etudiants";
        }
    }
}
