package com.login.LoginPage.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login.LoginPage.model.MenuItem;

@RestController
public class TestRequest {

	@GetMapping("/testAPI")
    public String testAPI() {
        System.out.println("Test service");
        return "menu-management";
    }
}
