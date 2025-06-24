package com.example.gestion_de_notes.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/structures")
@PreAuthorize("hasRole('ADMIN_SP')")
public class AdminStructuresController {
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Bienvenue dans l'administration des structures pédagogiques");
        model.addAttribute("userRole", "ADMIN_SP");
        return "admin/structures/dashboard";
    }
}
