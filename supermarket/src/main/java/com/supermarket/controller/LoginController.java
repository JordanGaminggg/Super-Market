package com.supermarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the Thymeleaf login page at GET /login.
 * Spring Security handles the actual POST /login form submission automatically.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null)  model.addAttribute("errorMsg",  "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");
        return "login";   // → src/main/resources/templates/login.html
    }
}
