package com.login.LoginPage.Controller;

import com.login.LoginPage.model.MenuItem;
import com.login.LoginPage.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/menu")
public class MenuManagementController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping
    public String listMenu(Model model) {
        model.addAttribute("items", menuItemRepository.findAll());
        model.addAttribute("newItem", new MenuItem());
        return "menu-management";
    }

    @PostMapping("/add")
    public String addMenuItem(@ModelAttribute MenuItem item) {
        menuItemRepository.save(item);
        return "redirect:/admin/menu";
    }

    @PostMapping("/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id) {
        menuItemRepository.findById(id).ifPresent(item -> {
            item.setAvailable(!item.isAvailable());
            menuItemRepository.save(item);
        });
        return "redirect:/admin/menu";
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return "redirect:/admin/menu";
    }
}