package com.phitrading.exchange.web.controller;

import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MarketChartsController {

    private static final Logger log = LoggerFactory.getLogger(MarketChartsController.class);

    private final PricingServiceClient pricingClient;

    public MarketChartsController(PricingServiceClient pricingClient) {
        this.pricingClient = pricingClient;
    }

    @GetMapping("/charts")
    public String charts(Model model) {
        List<InstrumentPriceDto> instruments = Collections.emptyList();
        try {
            instruments = pricingClient.getAllInstruments();
        } catch (Exception ex) {
            log.error("Failed to load instruments for charts page", ex);
        }

        // Choose default symbol: prefer AAPL if present, else first
        String defaultSymbol = instruments.stream()
                .map(InstrumentPriceDto::getSymbol)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(s -> !s.isBlank())
                .sorted()
                .findFirst()
                .orElse(null);

        Optional<String> aapl = instruments.stream()
                .map(InstrumentPriceDto::getSymbol)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter("AAPL"::equals)
                .findFirst();
        if (aapl.isPresent()) {
            defaultSymbol = aapl.get();
        }

        BigDecimal currentPrice = null;
        if (defaultSymbol != null) {
            try {
                InstrumentPriceDto priceDto = pricingClient.getCurrentPrice(defaultSymbol);
                currentPrice = priceDto != null ? priceDto.getLastPrice() : null;
            } catch (Exception ex) {
                log.warn("Failed to fetch current price for default symbol {}", defaultSymbol, ex);
            }
        }

        model.addAttribute("pageTitle", "Market Charts");
        model.addAttribute("symbols", instruments);
        model.addAttribute("selectedSymbol", defaultSymbol);
        model.addAttribute("currentPrice", currentPrice);
        return "charts";
    }

    @GetMapping(value = "/api/chart-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> chartData(@RequestParam(name = "symbol", required = false) String symbol,
                                       @RequestParam(name = "points", required = false, defaultValue = "60") Integer points) {
        if (symbol == null || symbol.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Parameter 'symbol' is required"));
        }
        String normalized = symbol.trim().toUpperCase();
        int safePoints = (points == null ? 60 : Math.max(10, Math.min(points, 1000)));

        // Validate symbol exists among instruments
        List<InstrumentPriceDto> instruments = Collections.emptyList();
        try {
            instruments = pricingClient.getAllInstruments();
        } catch (Exception ex) {
            log.error("Error loading instruments list for chart data", ex);
        }
        Set<String> available = instruments.stream()
                .map(InstrumentPriceDto::getSymbol)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        if (!available.contains(normalized)) {
            log.warn("Chart data requested for unknown symbol: {}", normalized);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown symbol: " + normalized));
        }

        try {
            InstrumentPriceDto priceDto = pricingClient.getCurrentPrice(normalized);
            BigDecimal startPrice = priceDto != null && priceDto.getLastPrice() != null
                    ? priceDto.getLastPrice()
                    : BigDecimal.valueOf(100);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("symbol", normalized);
            payload.put("currentPrice", startPrice);

            // Generate synthetic series around current price
            List<Map<String, Object>> series = new ArrayList<>(safePoints);
            BigDecimal prev = startPrice;
            Random rnd = new Random();
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < safePoints; i++) {
                // older to newer: i from 0..points-1 maps to time now - (points - i) minutes
                LocalDateTime ts = now.minusMinutes(safePoints - i);
                // randomChange in [-0.5%, +0.5%]
                double change = (rnd.nextDouble() - 0.5) / 100.0; // +/-0.5%
                BigDecimal factor = BigDecimal.valueOf(1.0 + change);
                BigDecimal price = prev.multiply(factor).setScale(4, RoundingMode.HALF_UP);
                Map<String, Object> point = new HashMap<>();
                point.put("timestamp", ts);
                point.put("price", price);
                series.add(point);
                prev = price;
            }
            payload.put("points", series);

            log.info("Chart data served: symbol={}, points={}", normalized, safePoints);
            return ResponseEntity.ok(payload);
        } catch (Exception ex) {
            log.error("Failed generating chart data for symbol {}", normalized, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate chart data: " + ex.getMessage()));
        }
    }
}
