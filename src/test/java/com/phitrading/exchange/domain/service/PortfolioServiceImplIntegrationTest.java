package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.PortfolioView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import phitrading.phitradingexchangemain.PhiTradingExchangeMainApplication;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PhiTradingExchangeMainApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PortfolioServiceImplIntegrationTest {

    @Autowired
    private UserAccountRepository userRepo;
    @Autowired
    private PortfolioPositionRepository positionRepo;
    @Autowired
    private PortfolioService portfolioService;

    @MockBean
    private PricingServiceClient pricingServiceClient; // mock remote pricing during integration test

    @Test
    void getUserPortfolioView_returnsSingleRowWithExpectedValues() {
        // Arrange: persist a user and one position
        UserAccount u = new UserAccount();
        u.setUsername("maria");
        u.setEmail("maria@example.com");
        u.setPasswordHash("x");
        userRepo.save(u);

        PortfolioPosition p = new PortfolioPosition();
        p.setUser(u);
        p.setSymbol("TSLA");
        p.setQuantity(5);
        p.setAveragePrice(new BigDecimal("200.0000"));
        positionRepo.save(p);

        // Mock price
        InstrumentPriceDto dto = new InstrumentPriceDto();
        dto.setSymbol("TSLA");
        dto.setName("Tesla Inc");
        dto.setLastPrice(new BigDecimal("210.00"));
        when(pricingServiceClient.getCurrentPrice("TSLA")).thenReturn(dto);

        // Act
        PortfolioView view = portfolioService.getUserPortfolioView("maria");

        // Assert
        assertThat(view.getRows()).hasSize(1);
        assertThat(view.getRows().get(0).getSymbol()).isEqualTo("TSLA");
        assertThat(view.getRows().get(0).getQuantity()).isEqualByComparingTo("5");
        assertThat(view.getRows().get(0).getAveragePrice()).isEqualByComparingTo("200.00");
        assertThat(view.getRows().get(0).getCurrentPrice()).isEqualByComparingTo("210.00");
    }
}
