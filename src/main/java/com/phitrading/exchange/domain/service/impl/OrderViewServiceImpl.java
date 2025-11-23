package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.entity.Order;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.service.OrderViewService;
import com.phitrading.exchange.web.dto.OrderRowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderViewServiceImpl implements OrderViewService {

    private static final Logger log = LoggerFactory.getLogger(OrderViewServiceImpl.class);

    private final OrderRepository orderRepository;

    public OrderViewServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderRowView> getUserOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when loading orders.");
        }

        List<Order> orders = orderRepository.findAllByUser_UsernameOrderByCreatedAtDesc(username);

        List<OrderRowView> rows = orders.stream()
                .map(OrderViewServiceImpl::toRow)
                .collect(Collectors.toList());

        log.info("Loaded orders for user={}, count={}", username, rows.size());
        return rows;
    }

    private static OrderRowView toRow(Order o) {
        LocalDateTime createdAt = o.getCreatedAt();
        LocalDateTime executedAt = o.getExecutedAt();

        BigDecimal qty = BigDecimal.valueOf(o.getQuantity());
        BigDecimal execPrice = o.getExecutionPrice();

        // Realized P/L: now stored on Order entity for SELL orders
        BigDecimal realized = o.getRealizedPnl();
        String realizedSign = "none";

        if (realized != null) {
            int cmp = realized.compareTo(BigDecimal.ZERO);
            realizedSign = cmp > 0 ? "positive" : (cmp < 0 ? "negative" : "zero");
        }

        return OrderRowView.builder()
                .symbol(o.getSymbol())
                .side(o.getSide() != null ? o.getSide().name() : null)
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .quantity(qty)
                .executionPrice(execPrice)
                .createdAt(createdAt)
                .executedAt(executedAt)
                .realizedPnl(realized)
                .realizedPnlSign(realizedSign)
                .build();
    }
}
