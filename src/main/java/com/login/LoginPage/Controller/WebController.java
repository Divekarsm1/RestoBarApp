package com.login.LoginPage.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Points to src/main/resources/templates/login.html
    }

    @GetMapping({"/", "/welcome"})
    public String welcome() {
        return "welcome"; // Points to src/main/resources/templates/welcome.html
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            // This clears all data stored in the session and marks it as invalid
            session.invalidate(); 
        }
        // Redirect to login page with a logout success message
        return "redirect:/login?logout=true"; 
    }
}