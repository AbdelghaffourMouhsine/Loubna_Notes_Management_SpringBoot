package com.example.gestion_de_notes.controller;

import com.example.gestion_de_notes.entity.CompteUtilisateur;
import com.example.gestion_de_notes.service.ImportEtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/notes/import")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class ImportEtudiantController {
    
    @Autowired
    private ImportEtudiantService importService;
    
    @GetMapping
    public String showImportForm(Model model) {
        return "admin/notes/import/form";
    }
    
    @PostMapping("/etudiants")
    public String importerEtudiants(@RequestParam("file") MultipartFile file,
                                   @RequestParam("anneeUniversitaire") String anneeUniversitaire,
                                   @AuthenticationPrincipal CompteUtilisateur utilisateur,
                                   RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
            return "redirect:/admin/notes/import";
        }
        
        ImportEtudiantService.ImportResult result = importService.importerEtudiants(file, anneeUniversitaire, utilisateur);
        
        if (result.isSuccess()) {
            String message = String.format("Import réussi: %d inscriptions, %d réinscriptions", 
                                          result.getNbInscriptions(), result.getNbReinscriptions());
            redirectAttributes.addFlashAttribute("success", message);
            
            if (!result.getWarnings().isEmpty()) {
                redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
            if (!result.getErrors().isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", result.getErrors());
            }
        }
        
        return "redirect:/admin/notes/import";
    }
}
