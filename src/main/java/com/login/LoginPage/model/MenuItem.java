package com.login.LoginPage.model;

import jakarta.persistence.*;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private String category;
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean available = true;

    public MenuItem() {}

    // ADD THIS GETTER
    public Long getId() {
        return id;
    }

    // ADD THESE SETTERS FOR THE ADD FORM
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() { return name; }
    public Double getPrice() { return price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}