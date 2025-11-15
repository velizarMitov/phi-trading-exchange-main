package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class OrdersController {

    @GetMapping("/orders")
    public String orders(@RequestParam(value = "status", required = false) String status, Model model) {
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("filter", status == null ? "ALL" : status.toUpperCase());
        model.addAttribute("orders", Collections.emptyList());
        return "orders";
    }
}
