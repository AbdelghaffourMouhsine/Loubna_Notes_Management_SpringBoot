package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.dto.CompteUtilisateurDTO;
import com.example.gestion_de_notes.dto.PersonneDTO;
import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.entity.Personne;
import com.example.gestion_de_notes.entity.Role;
import com.example.gestion_de_notes.service.CompteUtilisateurService;
import com.example.gestion_de_notes.service.PersonneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN_USER')")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final CompteUtilisateurService compteUtilisateurService;
    private final PersonneService personneService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<CompteUtilisateur> comptes = compteUtilisateurService.findAll();
        model.addAttribute("comptes", comptes);
        model.addAttribute("totalComptes", comptes.size());
        
        return "admin/users/dashboard";
    }
    
    @GetMapping("/personnes/search")
    public String searchPersonnes(@RequestParam(required = false) String nom,
                                 @RequestParam(required = false) String cin,
                                 @RequestParam(required = false) String searchTerm,
                                 Model model) {
        
        List<Personne> personnes;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            personnes = personneService.searchPersonnes(searchTerm);
        } else if ((nom != null && !nom.trim().isEmpty()) || (cin != null && !cin.trim().isEmpty())) {
            personnes = personneService.findByNomAndCin(nom, cin);
        } else {
            personnes = personneService.findAll();
        }
        
        model.addAttribute("personnes", personnes);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("nom", nom);
        model.addAttribute("cin", cin);
        
        return "admin/users/search-personnes";
    }
    
    @GetMapping("/personnes/new")
    public String showCreatePersonneForm(Model model) {
        model.addAttribute("personneDTO", new PersonneDTO());
        return "admin/users/create-personne";
    }
    
    @PostMapping("/personnes/create")
    public String createPersonne(@Valid @ModelAttribute PersonneDTO personneDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/users/create-personne";
        }
        
        try {
            Personne personne = personneService.save(personneDTO);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Personne créée avec succès: " + personne.getNomComplet());
            return "redirect:/admin/users/comptes/create?personneId=" + personne.getIdPersonne();
        } catch (IllegalArgumentException e) {
            result.rejectValue("cin", "error.personneDTO", e.getMessage());
            return "admin/users/create-personne";
        }
    }
    
    @GetMapping("/comptes/create")
    public String showCreateCompteForm(@RequestParam Long personneId, Model model) {
        Personne personne = personneService.findById(personneId)
            .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));
        
        // Vérifier les comptes existants pour cette personne
        List<CompteUtilisateur> comptesExistants = compteUtilisateurService.findByPersonneId(personneId);
        
        model.addAttribute("personne", personne);
        model.addAttribute("comptesExistants", comptesExistants);
        model.addAttribute("roles", Role.values());
        model.addAttribute("compteDTO", new CompteUtilisateurDTO());
        
        return "admin/users/create-compte";
    }
    
    @PostMapping("/comptes/create")
    public String createCompte(@RequestParam Long personneId,
                              @RequestParam Role role,
                              RedirectAttributes redirectAttributes) {
        
        try {
            CompteUtilisateurDTO compteDTO = compteUtilisateurService.createCompte(personneId, role);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Compte créé avec succès!");
            redirectAttributes.addFlashAttribute("newCompte", compteDTO);
            return "redirect:/admin/users/comptes/" + compteDTO.getIdCompte();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/comptes/create?personneId=" + personneId;
        }
    }
    
    @GetMapping("/comptes/{id}")
    public String showCompteDetails(@PathVariable Long id, Model model, @ModelAttribute("newCompte") CompteUtilisateurDTO newCompte) {
        CompteUtilisateur compte = compteUtilisateurService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé"));
        
        CompteUtilisateurDTO compteDTO = compteUtilisateurService.convertToDTO(compte);
        model.addAttribute("compte", compteDTO);
        model.addAttribute("roles", Role.values());
        // Ajout : si newCompte existe, on le remet dans le modèle
        if (newCompte != null && newCompte.getIdCompte() != null) {
            model.addAttribute("newCompte", newCompte);
        }
        
        return "admin/users/compte-details";
    }
    
    @PostMapping("/comptes/{id}/update-role")
    public String updateRole(@PathVariable Long id,
                           @RequestParam Role role,
                           RedirectAttributes redirectAttributes) {
        
        try {
            compteUtilisateurService.updateRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Rôle mis à jour avec succès");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/users/comptes/" + id;
    }
    
    @PostMapping("/comptes/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = compteUtilisateurService.resetPassword(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Mot de passe réinitialisé avec succès");
            redirectAttributes.addFlashAttribute("newPassword", newPassword);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/users/comptes/" + id;
    }
    
    @PostMapping("/comptes/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CompteUtilisateur compte = compteUtilisateurService.toggleEnabled(id);
            String message = compte.getEnabled() ? "Compte activé" : "Compte désactivé";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/users/comptes/" + id;
    }
    
    @PostMapping("/comptes/{id}/toggle-locked")
    public String toggleLocked(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CompteUtilisateur compte = compteUtilisateurService.toggleLocked(id);
            String message = compte.getLocked() ? "Compte verrouillé" : "Compte déverrouillé";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/users/comptes/" + id;
    }
}
