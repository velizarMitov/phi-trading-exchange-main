package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.common.exception.InsufficientFundsException;
import com.phitrading.exchange.domain.entity.Order;
import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplUnitTest {

    @Mock
    private PricingServiceClient pricingServiceClient;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PortfolioPositionRepository portfolioPositionRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPasswordHash("x");
        user.setCashBalance(new BigDecimal("1000.00"));
    }

    @Test
    void buy_createsExecutedOrder_updatesCash_andPosition() {
        // Arrange
        InstrumentPriceDto priceDto = new InstrumentPriceDto();
        priceDto.setSymbol("AAPL");
        priceDto.setLastPrice(new BigDecimal("100.00"));

        when(pricingServiceClient.getCurrentPrice("AAPL")).thenReturn(priceDto);
        when(userAccountRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(portfolioPositionRepository.findByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());

        // Act
        tradeService.buy("john", "AAPL", 2);

        // Assert cash deducted: 2 * 100 = 200
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getCashBalance()).isEqualByComparingTo(new BigDecimal("800.0000"));

        // Assert position saved with new quantity and average price
        ArgumentCaptor<PortfolioPosition> posCaptor = ArgumentCaptor.forClass(PortfolioPosition.class);
        verify(portfolioPositionRepository).save(posCaptor.capture());
        PortfolioPosition savedPos = posCaptor.getValue();
        assertThat(savedPos.getSymbol()).isEqualTo("AAPL");
        assertThat(savedPos.getQuantity()).isEqualTo(2);
        assertThat(savedPos.getAveragePrice()).isEqualByComparingTo(new BigDecimal("100.0000"));

        // Assert order saved with EXECUTED status and correct fields
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getSide()).isEqualTo(Order.OrderSide.BUY);
        assertThat(savedOrder.getStatus()).isEqualTo(Order.OrderStatus.EXECUTED);
        assertThat(savedOrder.getSymbol()).isEqualTo("AAPL");
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getExecutionPrice()).isEqualByComparingTo(new BigDecimal("100.00"));

        verifyNoMoreInteractions(orderRepository, portfolioPositionRepository, userAccountRepository, pricingServiceClient);
    }

    @Test
    void buy_throwsWhenInsufficientFunds() {
        // Arrange price high so cost > cash
        InstrumentPriceDto priceDto = new InstrumentPriceDto();
        priceDto.setSymbol("NVDA");
        priceDto.setLastPrice(new BigDecimal("600.00"));
        when(pricingServiceClient.getCurrentPrice("NVDA")).thenReturn(priceDto);
        when(userAccountRepository.findByUsername("john")).thenReturn(Optional.of(user));

        // Act + Assert
        assertThrows(InsufficientFundsException.class, () -> tradeService.buy("john", "NVDA", 2));

        verify(userAccountRepository, never()).save(any());
        verify(portfolioPositionRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }
}
