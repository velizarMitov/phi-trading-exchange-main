package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.OrderViewService;
import com.phitrading.exchange.web.dto.OrderRowView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderExportControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private OrderViewService orderViewService;

    @InjectMocks
    private OrderExportController controller;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCsv_shouldReturnCsvWithHeaderAndAttachment() throws Exception {
        String username = "test.user";
        when(orderViewService.getUserOrders(username)).thenReturn(List.of(
                OrderRowView.builder()
                        .symbol("AAPL")
                        .side("BUY")
                        .status("EXECUTED")
                        .quantity(BigDecimal.TEN)
                        .executionPrice(new BigDecimal("123.45"))
                        .createdAt(LocalDateTime.now())
                        .executedAt(LocalDateTime.now())
                        .build()
        ));

        var mvcResult = mockMvc.perform(get("/orders/export/csv").principal(() -> username))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        assertThat(body).contains("Date,Symbol,Side,Quantity,Price,Status");

        String cd = mvcResult.getResponse().getHeader("Content-Disposition");
        assertThat(cd).isNotNull();
        assertThat(cd).contains("attachment;");
        assertThat(cd).contains("orders-");
        assertThat(cd).contains(".csv");
    }
}
