package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.OrderViewService;
import com.phitrading.exchange.web.dto.OrderRowView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderExportControllerWebMvcTest {

    private MockMvc mockMvc;
    private OrderViewService orderViewService;

    @BeforeEach
    void setup() {
        orderViewService = Mockito.mock(OrderViewService.class);
        OrderExportController controller = new OrderExportController(orderViewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void exportCsv_shouldReturnCsvWithHeaderAndData() throws Exception {
        // Arrange sample data
        OrderRowView row = OrderRowView.builder()
                .symbol("AAPL")
                .side("BUY")
                .status("EXECUTED")
                .quantity(new BigDecimal("10"))
                .executionPrice(new BigDecimal("150.25"))
                .createdAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now())
                .build();
        Mockito.when(orderViewService.getUserOrders(anyString())).thenReturn(List.of(row));

        // Act + Assert
        mockMvc.perform(get("/orders/export/csv").principal(() -> "testUser"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=\"orders-testUser.csv\"")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("Date,Symbol,Side,Quantity,Price,Status")));
    }
}
