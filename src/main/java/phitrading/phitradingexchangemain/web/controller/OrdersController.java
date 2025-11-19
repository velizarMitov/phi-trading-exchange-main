package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.OrderViewService;
import com.phitrading.exchange.web.dto.OrderRowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderViewService orderViewService;

    public OrdersController(OrderViewService orderViewService) {
        this.orderViewService = orderViewService;
    }

    @GetMapping("/orders")
    public String orders(Principal principal, Model model) {
        String username = principal != null ? principal.getName() : null;
        List<OrderRowView> orders = orderViewService.getUserOrders(username);
        log.info("Loading orders for user={}, count={}", username, orders.size());
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("orders", orders);
        return "orders";
    }
}
