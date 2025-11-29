package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.domain.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TradeControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private TradeService tradeService;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private TradeController tradeController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tradeController).build();
    }

    @Test
    void postBuy_whenValid_shouldRedirectToPortfolio() throws Exception {
        doNothing().when(tradeService).buy("testUser", "AAPL", 3L);

        mockMvc.perform(post("/trade/buy")
                        .principal(() -> "testUser")
                        .param("symbol", "AAPL")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portfolio"));

        verify(tradeService).buy("testUser", "AAPL", 3L);
    }
}
