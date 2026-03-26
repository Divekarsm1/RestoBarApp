package com.login.LoginPage.repository;


import com.login.LoginPage.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    // This allows you to see the latest bills first in history
    List<Bill> findAllByOrderByCheckoutTimeDesc();
}
