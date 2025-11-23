package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.OrderRowView;

import java.util.List;

public interface OrderViewService {
    List<OrderRowView> getUserOrders(String username);
}
