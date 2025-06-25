package com.example.gestion_de_notes.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class RedirectDashboardController {
    @GetMapping("/redirect-dashboard")
    public String redirectDashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN_USER")) {
                return "redirect:/admin/users/dashboard";
            } else if (role.equals("ROLE_ADMIN_NOTES")) {
                return "redirect:/admin/notes/dashboard";
            } else if (role.equals("ROLE_ADMIN_SP")) {
                return "redirect:/admin/structures/dashboard";
            }
        }
        return "redirect:/login";
    }
}
