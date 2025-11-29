package com.phitrading.exchange.domain.repository;

import com.phitrading.exchange.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Fetch orders for a user by username, most recent first
    List<Order> findAllByUser_UsernameOrderByCreatedAtDesc(String username);

    // Latest 3 executed orders for dashboard
    List<Order> findTop3ByUser_UsernameAndStatusOrderByExecutedAtDesc(String username, Order.OrderStatus status);

    // Count executed orders for a user
    long countByUser_UsernameAndStatus(String username, Order.OrderStatus status);

    // Check if any order exists for a symbol (used by admin delete guard)
    boolean existsBySymbol(String symbol);

    // Count orders for a given symbol (used for safe delete messaging)
    long countBySymbol(String symbol);
}
