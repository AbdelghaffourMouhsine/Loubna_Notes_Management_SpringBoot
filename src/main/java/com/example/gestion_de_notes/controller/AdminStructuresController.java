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
    
    @Autowired
    private ParametreValidationService parametreValidationService;
    
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
    
    @GetMapping("/structure")
    public String structureHierarchique(Model model) {
        List<FiliereDTO> filieres = filiereService.findAll();
        List<NiveauDTO> niveaux = niveauService.findAll();
        List<ModuleDTO> modules = moduleService.findAll();
        List<ElementDTO> elements = elementService.findAll();
        
        model.addAttribute("filieres", filieres);
        model.addAttribute("niveaux", niveaux);
        model.addAttribute("modules", modules);
        model.addAttribute("elements", elements);
        
        return "admin/structures/structure-hierarchique";
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
    
    @GetMapping("/modules/new")
    public String showCreateModuleForm(Model model) {
        model.addAttribute("module", new ModuleDTO());
        model.addAttribute("niveaux", niveauService.findAll());
        model.addAttribute("enseignants", personneService.findAll());
        return "admin/structures/modules/form";
    }
    
    @PostMapping("/modules")
    public String createModule(@Valid @ModelAttribute("module") ModuleDTO moduleDTO,
                              BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (moduleService.existsByCode(moduleDTO.getCode())) {
            result.rejectValue("code", "error.module", "Ce code existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("niveaux", niveauService.findAll());
            model.addAttribute("enseignants", personneService.findAll());
            return "admin/structures/modules/form";
        }
        
        moduleService.save(moduleDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Module créé avec succès");
        return "redirect:/admin/structures/modules";
    }
    
    @GetMapping("/modules/{id}/edit")
    public String showEditModuleForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ModuleDTO> module = moduleService.findById(id);
        if (module.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Module non trouvé");
            return "redirect:/admin/structures/modules";
        }
        
        model.addAttribute("module", module.get());
        model.addAttribute("niveaux", niveauService.findAll());
        model.addAttribute("enseignants", personneService.findAll());
        return "admin/structures/modules/form";
    }
    
    @PostMapping("/modules/{id}")
    public String updateModule(@PathVariable Long id, @Valid @ModelAttribute("module") ModuleDTO moduleDTO,
                              BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (moduleService.existsByCodeAndIdNot(moduleDTO.getCode(), id)) {
            result.rejectValue("code", "error.module", "Ce code existe déjà");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("niveaux", niveauService.findAll());
            model.addAttribute("enseignants", personneService.findAll());
            return "admin/structures/modules/form";
        }
        
        moduleDTO.setIdModule(id);
        moduleService.save(moduleDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Module modifié avec succès");
        return "redirect:/admin/structures/modules";
    }
    
    @PostMapping("/modules/{id}/delete")
    public String deleteModule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            moduleService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Module supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce module. Il est peut-être utilisé.");
        }
        return "redirect:/admin/structures/modules";
    }
    
    // ===============================
    // GESTION DES ELEMENTS
    // ===============================
    
    @GetMapping("/elements")
    public String listElements(Model model) {
        List<ElementDTO> elements = elementService.findAll();
        model.addAttribute("elements", elements);
        return "admin/structures/elements/list";
    }
    
    @GetMapping("/elements/new")
    public String showCreateElementForm(Model model) {
        model.addAttribute("element", new ElementDTO());
        model.addAttribute("modules", moduleService.findAll());
        return "admin/structures/elements/form";
    }
    
    @PostMapping("/elements")
    public String createElement(@Valid @ModelAttribute("element") ElementDTO elementDTO,
                               BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("modules", moduleService.findAll());
            return "admin/structures/elements/form";
        }
        
        elementService.save(elementDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Élément créé avec succès");
        return "redirect:/admin/structures/elements";
    }
    
    @GetMapping("/elements/{id}/edit")
    public String showEditElementForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ElementDTO> element = elementService.findById(id);
        if (element.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Élément non trouvé");
            return "redirect:/admin/structures/elements";
        }
        
        model.addAttribute("element", element.get());
        model.addAttribute("modules", moduleService.findAll());
        return "admin/structures/elements/form";
    }
    
    @PostMapping("/elements/{id}")
    public String updateElement(@PathVariable Long id, @Valid @ModelAttribute("element") ElementDTO elementDTO,
                               BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("modules", moduleService.findAll());
            return "admin/structures/elements/form";
        }
        
        elementDTO.setIdElement(id);
        elementService.save(elementDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Élément modifié avec succès");
        return "redirect:/admin/structures/elements";
    }
    
    @PostMapping("/elements/{id}/delete")
    public String deleteElement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            elementService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Élément supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer cet élément. Il est peut-être utilisé.");
        }
        return "redirect:/admin/structures/elements";
    }
    
    // ===============================
    // GESTION DES AFFECTATIONS
    // ===============================
    
    @GetMapping("/affectations")
    public String listAffectations(Model model) {
        List<EnseignantModuleAnneeDTO> affectations = enseignantModuleAnneeService.findAll();
        model.addAttribute("affectations", affectations);
        return "admin/structures/affectations/list";
    }
    
    @GetMapping("/affectations/new")
    public String showCreateAffectationForm(Model model) {
        model.addAttribute("affectation", new EnseignantModuleAnneeDTO());
        model.addAttribute("enseignants", personneService.findAll());
        model.addAttribute("modules", moduleService.findAll());
        model.addAttribute("elements", elementService.findAll());
        return "admin/structures/affectations/form";
    }
    
    @PostMapping("/affectations")
    public String createAffectation(@Valid @ModelAttribute("affectation") EnseignantModuleAnneeDTO affectationDTO,
                                   BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("enseignants", personneService.findAll());
            model.addAttribute("modules", moduleService.findAll());
            model.addAttribute("elements", elementService.findAll());
            return "admin/structures/affectations/form";
        }
        
        enseignantModuleAnneeService.save(affectationDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Affectation créée avec succès");
        return "redirect:/admin/structures/affectations";
    }
    
    @GetMapping("/affectations/{id}/edit")
    public String showEditAffectationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EnseignantModuleAnneeDTO> affectation = enseignantModuleAnneeService.findById(id);
        if (affectation.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Affectation non trouvée");
            return "redirect:/admin/structures/affectations";
        }
        
        model.addAttribute("affectation", affectation.get());
        model.addAttribute("enseignants", personneService.findAll());
        model.addAttribute("modules", moduleService.findAll());
        model.addAttribute("elements", elementService.findAll());
        return "admin/structures/affectations/form";
    }
    
    @PostMapping("/affectations/{id}")
    public String updateAffectation(@PathVariable Long id, @Valid @ModelAttribute("affectation") EnseignantModuleAnneeDTO affectationDTO,
                                   BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("enseignants", personneService.findAll());
            model.addAttribute("modules", moduleService.findAll());
            model.addAttribute("elements", elementService.findAll());
            return "admin/structures/affectations/form";
        }
        
        affectationDTO.setId(id);
        enseignantModuleAnneeService.save(affectationDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Affectation modifiée avec succès");
        return "redirect:/admin/structures/affectations";
    }
    
    @PostMapping("/affectations/{id}/delete")
    public String deleteAffectation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            enseignantModuleAnneeService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Affectation supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer cette affectation.");
        }
        return "redirect:/admin/structures/affectations";
    }
    
    // ===============================
    // GESTION DES PARAMETRES DE VALIDATION
    // ===============================
    
    @GetMapping("/parametres-validation")
    public String listParametresValidation(Model model) {
        List<ParametreValidationDTO> parametres = parametreValidationService.findAll();
        model.addAttribute("parametres", parametres);
        return "admin/structures/parametres-validation/list";
    }
    
    @GetMapping("/parametres-validation/new")
    public String showCreateParametreValidationForm(Model model) {
        model.addAttribute("parametre", new ParametreValidationDTO());
        model.addAttribute("filieres", filiereService.findAll());
        model.addAttribute("niveaux", niveauService.findAll());
        return "admin/structures/parametres-validation/form";
    }
    
    @PostMapping("/parametres-validation")
    public String createParametreValidation(@Valid @ModelAttribute("parametre") ParametreValidationDTO parametreDTO,
                                          BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        // Validation métier
        if (parametreDTO.getSeuilValidationRattrapage() != null && parametreDTO.getSeuilValidationNormale() != null
                && parametreDTO.getSeuilValidationRattrapage().compareTo(parametreDTO.getSeuilValidationNormale()) > 0) {
            result.rejectValue("seuilValidationRattrapage", "error.parametre", 
                "Le seuil de rattrapage ne peut pas être supérieur au seuil normal");
        }
        
        if (parametreValidationService.existsByFiliereAndNiveau(parametreDTO.getIdFiliere(), parametreDTO.getIdNiveau())) {
            result.rejectValue("idNiveau", "error.parametre", 
                "Un paramètre de validation existe déjà pour cette combinaison filière/niveau");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("filieres", filiereService.findAll());
            model.addAttribute("niveaux", niveauService.findAll());
            return "admin/structures/parametres-validation/form";
        }
        
        try {
            parametreValidationService.save(parametreDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Paramètre de validation créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }
        
        return "redirect:/admin/structures/parametres-validation";
    }
    
    @GetMapping("/parametres-validation/{id}/edit")
    public String showEditParametreValidationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ParametreValidationDTO> parametre = parametreValidationService.findById(id);
        if (parametre.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Paramètre de validation non trouvé");
            return "redirect:/admin/structures/parametres-validation";
        }
        
        model.addAttribute("parametre", parametre.get());
        model.addAttribute("filieres", filiereService.findAll());
        model.addAttribute("niveaux", niveauService.findAll());
        return "admin/structures/parametres-validation/form";
    }
    
    @PostMapping("/parametres-validation/{id}")
    public String updateParametreValidation(@PathVariable Long id, 
                                          @Valid @ModelAttribute("parametre") ParametreValidationDTO parametreDTO,
                                          BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        
        // Validation métier
        if (parametreDTO.getSeuilValidationRattrapage() != null && parametreDTO.getSeuilValidationNormale() != null
                && parametreDTO.getSeuilValidationRattrapage().compareTo(parametreDTO.getSeuilValidationNormale()) > 0) {
            result.rejectValue("seuilValidationRattrapage", "error.parametre", 
                "Le seuil de rattrapage ne peut pas être supérieur au seuil normal");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("filieres", filiereService.findAll());
            model.addAttribute("niveaux", niveauService.findAll());
            return "admin/structures/parametres-validation/form";
        }
        
        try {
            parametreDTO.setIdParametre(id);
            parametreValidationService.save(parametreDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Paramètre de validation modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }
        
        return "redirect:/admin/structures/parametres-validation";
    }
    
    @PostMapping("/parametres-validation/{id}/delete")
    public String deleteParametreValidation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parametreValidationService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Paramètre de validation supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce paramètre de validation.");
        }
        return "redirect:/admin/structures/parametres-validation";
    }
    
    // ===============================
    // API ENDPOINTS POUR LES NIVEAUX PAR FILIERE
    // ===============================
    
    @GetMapping("/api/filieres/{filiereId}/niveaux")
    @ResponseBody
    public ResponseEntity<List<NiveauDTO>> getNiveauxByFiliere(@PathVariable Long filiereId) {
        try {
            List<NiveauDTO> niveaux = niveauService.findByFiliereId(filiereId);
            return ResponseEntity.ok(niveaux);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
