package com.example.gestion_de_notes.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notes")
@PreAuthorize("hasRole('ADMIN_NOTES')")
public class AdminNotesController {
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Bienvenue dans l'administration des notes");
        model.addAttribute("userRole", "ADMIN_NOTES");
        return "admin/notes/dashboard";
    }
}
