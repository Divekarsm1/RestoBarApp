package com.login.LoginPage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login.LoginPage.order.RestaurantOrder;

public interface OrderRepository extends JpaRepository<RestaurantOrder, Long> {
}
