/*
 * package com.login.LoginPage.Security;
 * 
 * 
 * import org.springframework.boot.CommandLineRunner; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration;
 * 
 * import com.login.LoginPage.model.MenuItem; import
 * com.login.LoginPage.repository.MenuItemRepository;
 * 
 * @Configuration public class DataInitializer {
 * 
 * @Bean CommandLineRunner initDatabase(MenuItemRepository repository) { return
 * args -> { if (repository.count() == 0) { repository.save(new
 * MenuItem("Classic Burger", 12.50, "Main")); repository.save(new
 * MenuItem("Margherita Pizza", 14.00, "Main")); repository.save(new
 * MenuItem("French Fries", 4.50, "Sides")); repository.save(new
 * MenuItem("Coke", 2.50, "Drinks")); repository.save(new
 * MenuItem("Pasta Carbonara", 15.00, "Main")); repository.save(new
 * MenuItem("Checken 1", 15.00, "Main")); repository.save(new
 * MenuItem("Checken Carbonara", 15.00, "Main")); repository.save(new
 * MenuItem("Pasta Checken", 15.00, "Main")); repository.save(new
 * MenuItem("1 Carbonara", 15.00, "Main")); repository.save(new
 * MenuItem("Pasta 1", 15.00, "Main")); repository.save(new MenuItem("Checken",
 * 15.00, "Main")); repository.save(new MenuItem("Checken Masala", 15.00,
 * "Main")); System.out.println("Menu seeded to database."); } }; } }
 */