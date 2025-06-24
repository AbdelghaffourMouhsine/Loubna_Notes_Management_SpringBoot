package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.FiliereDTO;
import com.example.gestion_de_notes.service.FiliereService;
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
@RequestMapping("/admin/structures/filieres")
@PreAuthorize("hasRole('ADMIN_SP')")
public class FiliereController {
    
    @Autowired
    private FiliereService filiereService;
    
    @GetMapping
    public String listFilieres(Model model) {
        List<FiliereDTO> filieres = filiereService.findAll();
        model.addAttribute("filieres", filieres);
        return "admin/structures/filieres/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("filiereDTO", new FiliereDTO());
        return "admin/structures/filieres/form";
    }    
    @PostMapping
    public String createFiliere(@Valid @ModelAttribute FiliereDTO filiereDTO, 
                               BindingResult result, 
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/structures/filieres/form";
        }
        
        if (filiereService.existsByAlias(filiereDTO.getAlias())) {
            result.rejectValue("alias", "error.filiere", "Une filière avec cet alias existe déjà");
            return "admin/structures/filieres/form";
        }
        
        filiereService.save(filiereDTO);
        redirectAttributes.addFlashAttribute("success", "Filière créée avec succès");
        return "redirect:/admin/structures/filieres";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FiliereDTO> filiere = filiereService.findById(id);
        if (filiere.isPresent()) {
            model.addAttribute("filiereDTO", filiere.get());
            return "admin/structures/filieres/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Filière non trouvée");
            return "redirect:/admin/structures/filieres";
        }
    }
    
    @PostMapping("/{id}")
    public String updateFiliere(@PathVariable Long id, 
                               @Valid @ModelAttribute FiliereDTO filiereDTO,
                               BindingResult result, 
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/structures/filieres/form";
        }
        
        filiereDTO.setIdFiliere(id);
        filiereService.save(filiereDTO);
        redirectAttributes.addFlashAttribute("success", "Filière modifiée avec succès");
        return "redirect:/admin/structures/filieres";
    }
    
    @DeleteMapping("/{id}")
    public String deleteFiliere(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            filiereService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Filière supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer la filière");
        }
        return "redirect:/admin/structures/filieres";
    }
}
