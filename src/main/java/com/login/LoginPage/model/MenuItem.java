package com.login.LoginPage.model;


import jakarta.persistence.*;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private String category; // e.g., "Starters", "Main", "Drinks"

    // Constructors, Getters, and Setters
    public MenuItem() {}
    public MenuItem(String name, Double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getName() { return name; }
    public Double getPrice() { return price; }
}
