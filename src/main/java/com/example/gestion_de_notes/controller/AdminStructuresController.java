package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.*;
import com.example.gestion_de_notes.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/structures")
@PreAuthorize("hasRole('ADMIN_SP')")
public class AdminStructuresController {
    
    @Autowired
    private FiliereService filiereService;
    
    @Autowired
    private NiveauService niveauService;
    
    @Autowired
    private ModuleService moduleService;
    
    @Autowired
    private ElementService elementService;
    
    @Autowired
    private EnseignantModuleAnneeService enseignantModuleAnneeService;
    
    @Autowired
    private PersonneService personneService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Bienvenue dans l'administration des structures pédagogiques");
        model.addAttribute("userRole", "ADMIN_SP");
        
        // Statistiques pour le dashboard
        model.addAttribute("totalFilieres", filiereService.findAll().size());
        model.addAttribute("totalNiveaux", niveauService.findAll().size());
        model.addAttribute("totalModules", moduleService.findAll().size());
        model.addAttribute("totalElements", elementService.findAll().size());
        
        return "admin/structures/dashboard";
    }    
    // ===============================
    // GESTION DES FILIERES
    // ===============================
    
    @GetMapping("/filieres")
    public String listFilieres(Model model) {
        List<FiliereDTO> filieres = filiereService.findAll();
        model.addAttribute("filieres", filieres);
        return "admin/structures/filieres/list";
    }
    
    @GetMapping("/filieres/new")
    public String showCreateFiliereForm(Model model) {
        model.addAttribute("filiere", new FiliereDTO());
        model.addAttribute("enseignants", personneService.findAll());
        return "admin/structures/filieres/form";
    }
    
    @PostMapping("/filieres")
    public String createFiliere(@Valid @ModelAttribute("filiere") FiliereDTO filiereDTO,
                               BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (filiereService.existsByAlias(filiereDTO.getAlias())) {
            result.rejectValue("alias", "error.filiere", "Cet alias existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("enseignants", personneService.findAll());
            return "admin/structures/filieres/form";
        }
        
        filiereService.save(filiereDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Filière créée avec succès");
        return "redirect:/admin/structures/filieres";
    }
    
    @GetMapping("/filieres/{id}/edit")
    public String showEditFiliereForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FiliereDTO> filiere = filiereService.findById(id);
        if (filiere.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Filière non trouvée");
            return "redirect:/admin/structures/filieres";
        }
        
        model.addAttribute("filiere", filiere.get());
        model.addAttribute("enseignants", personneService.findAll());
        return "admin/structures/filieres/form";
    }
    
    @PostMapping("/filieres/{id}")
    public String updateFiliere(@PathVariable Long id, @Valid @ModelAttribute("filiere") FiliereDTO filiereDTO,
                               BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (filiereService.existsByAliasAndIdNot(filiereDTO.getAlias(), id)) {
            result.rejectValue("alias", "error.filiere", "Cet alias existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("enseignants", personneService.findAll());
            return "admin/structures/filieres/form";
        }
        
        filiereDTO.setIdFiliere(id);
        filiereService.save(filiereDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Filière modifiée avec succès");
        return "redirect:/admin/structures/filieres";
    }
    
    @PostMapping("/filieres/{id}/delete")
    public String deleteFiliere(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            filiereService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Filière supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer cette filière. Elle est peut-être utilisée.");
        }
        return "redirect:/admin/structures/filieres";
    }
    
    // ===============================
    // GESTION DES NIVEAUX
    // ===============================
    
    @GetMapping("/niveaux")
    public String listNiveaux(Model model) {
        List<NiveauDTO> niveaux = niveauService.findAll();
        model.addAttribute("niveaux", niveaux);
        return "admin/structures/niveaux/list";
    }
    
    @GetMapping("/niveaux/new")
    public String showCreateNiveauForm(Model model) {
        model.addAttribute("niveau", new NiveauDTO());
        model.addAttribute("niveauxSuivants", niveauService.findAll());
        model.addAttribute("filieres", filiereService.findAll());
        return "admin/structures/niveaux/form";
    }
    
    @PostMapping("/niveaux")
    public String createNiveau(@Valid @ModelAttribute("niveau") NiveauDTO niveauDTO,
                              BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (niveauService.existsByAlias(niveauDTO.getAlias())) {
            result.rejectValue("alias", "error.niveau", "Cet alias existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("niveauxSuivants", niveauService.findAll());
            model.addAttribute("filieres", filiereService.findAll());
            return "admin/structures/niveaux/form";
        }
        
        niveauService.save(niveauDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Niveau créé avec succès");
        return "redirect:/admin/structures/niveaux";
    }
    
    @GetMapping("/niveaux/{id}/edit")
    public String showEditNiveauForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<NiveauDTO> niveau = niveauService.findById(id);
        if (niveau.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Niveau non trouvé");
            return "redirect:/admin/structures/niveaux";
        }
        
        model.addAttribute("niveau", niveau.get());
        model.addAttribute("niveauxSuivants", niveauService.findAll());
        model.addAttribute("filieres", filiereService.findAll());
        return "admin/structures/niveaux/form";
    }
    
    @PostMapping("/niveaux/{id}")
    public String updateNiveau(@PathVariable Long id, @Valid @ModelAttribute("niveau") NiveauDTO niveauDTO,
                              BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (niveauService.existsByAliasAndIdNot(niveauDTO.getAlias(), id)) {
            result.rejectValue("alias", "error.niveau", "Cet alias existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("niveauxSuivants", niveauService.findAll());
            model.addAttribute("filieres", filiereService.findAll());
            return "admin/structures/niveaux/form";
        }
        
        niveauDTO.setIdNiveau(id);
        niveauService.save(niveauDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Niveau modifié avec succès");
        return "redirect:/admin/structures/niveaux";
    }
    
    @PostMapping("/niveaux/{id}/delete")
    public String deleteNiveau(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            niveauService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Niveau supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce niveau. Il est peut-être utilisé.");
        }
        return "redirect:/admin/structures/niveaux";
    }
    
    // ===============================
    // GESTION DES MODULES
    // ===============================
    
    @GetMapping("/modules")
    public String listModules(Model model) {
        List<ModuleDTO> modules = moduleService.findAll();
        model.addAttribute("modules", modules);
        return "admin/structures/modules/list";
    }
}
