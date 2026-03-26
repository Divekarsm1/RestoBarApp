package com.login.LoginPage.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.login.LoginPage.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
