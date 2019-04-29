package com.silverbars.liveorder.service;

import com.silverbars.liveorder.service.domain.LiveOrderSummary;
import com.silverbars.liveorder.service.domain.LiveOrderSummaryRecord;
import com.silverbars.liveorder.service.domain.OrderType;
import com.silverbars.liveorder.service.domain.SilverBarOrder;
import com.silverbars.liveorder.service.repository.SilverBarOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.silverbars.liveorder.service.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SilverBarOrderServiceImplTest {

    private final static String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Mock
    private SilverBarOrderRepository repository;

    @InjectMocks
    private SilverBarOrderServiceImpl service;

    @Test
    void shouldRegisterNewOrder() {
        // when
        service.registerOrder(someNewSilverBarOrder());

        // then
        var captor = ArgumentCaptor.forClass(SilverBarOrder.class);
        verify(repository).saveOrder(captor.capture());
        var savedOrder = captor.getValue();
        assertThat(savedOrder.getOrderId())
                .isNotNull()
                .matches(UUID_REGEX);
    }

    @ParameterizedTest
    @MethodSource("invalidBarOrders")
    void willThrowExceptionIfSavingInvalidOrder(SilverBarOrder invalidOrder, String validationMessage) {
        assertThatThrownBy(
                // when
                () -> service.registerOrder(invalidOrder))

                // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(validationMessage);
    }

    private static Stream<Arguments> invalidBarOrders() {
        return Stream.of(
                Arguments.of(silverBarOrder(someString(), someString(), someOrderType(), someBigDecimal(), someInt()), "orderId cannot be present for the new record"),
                Arguments.of(silverBarOrder(null, null, someOrderType(), someBigDecimal(), someInt()), "userId is not present"),
                Arguments.of(silverBarOrder(null, someString(), null, someBigDecimal(), someInt()), "orderType is not present"),
                Arguments.of(silverBarOrder(null, someString(), someOrderType(), null, someInt()), "thousandGramsPrice is not present")
        );
    }

    @Test
    void shouldCancelOrder() {
        // given
        var orderId = someUUID();

        // when
        service.cancelRegisteredOrder(orderId);

        // then
        verify(repository).cancelOrder(orderId);
    }

    @Test
    void shouldReturnLiveOrderSummary() {
        // given
        when(repository.getAllOrders()).thenReturn(Collections.emptyList());

        // when
        LiveOrderSummary liveOrderSummary = service.getLiveOrderSummary();

        // then
        assertThat(liveOrderSummary).isNotNull();
    }

    @Test
    void shouldReturnLiveOrderSummaryGroupedByOrderType() {
        // given
        SilverBarOrder buyOrder = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, someBigDecimal(), someInt());
        SilverBarOrder sellOrder = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, someBigDecimal(), someInt());
        when(repository.getAllOrders()).thenReturn(List.of(buyOrder, sellOrder));

        // when
        LiveOrderSummary liveOrderSummary = service.getLiveOrderSummary();

        // then
        assertThat(liveOrderSummary.getBuyOrders()).hasSize(1);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getBuyOrders(), buyOrder.getThousandGramsPrice(), buyOrder.getWeightInGrams(), buyOrder);
        assertThat(liveOrderSummary.getSellOrders()).hasSize(1);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getSellOrders(), sellOrder.getThousandGramsPrice(), sellOrder.getWeightInGrams(), sellOrder);
    }

    @Test
    void shouldSumOrdersWithTheSamePrice() {
        // given
        BigDecimal samePriceBuy = someBigDecimal();
        SilverBarOrder buyOrder1 = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, samePriceBuy, someInt());
        SilverBarOrder buyOrder2 = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, samePriceBuy, someInt());
        SilverBarOrder buyOrder3 = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, samePriceBuy.add(BigDecimal.TEN), someInt());

        BigDecimal samePriceSell = someBigDecimal();
        SilverBarOrder sellOrder1 = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, samePriceSell, someInt());
        SilverBarOrder sellOrder2 = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, samePriceSell, someInt());
        SilverBarOrder sellOrder3 = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, samePriceSell.add(BigDecimal.TEN), someInt());

        when(repository.getAllOrders()).thenReturn(List.of(buyOrder1, sellOrder1, buyOrder3, buyOrder2, sellOrder2, sellOrder3));

        // when
        LiveOrderSummary liveOrderSummary = service.getLiveOrderSummary();

        // then
        assertThat(liveOrderSummary.getBuyOrders()).hasSize(2);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getBuyOrders(), samePriceBuy, buyOrder1.getWeightInGrams() + buyOrder2.getWeightInGrams(), buyOrder1, buyOrder2);
        assertLiveOrderRecordOnPosition(1, liveOrderSummary.getBuyOrders(), buyOrder3.getThousandGramsPrice(), buyOrder3.getWeightInGrams(), buyOrder3);
        assertThat(liveOrderSummary.getSellOrders()).hasSize(2);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getSellOrders(), sellOrder3.getThousandGramsPrice(), sellOrder3.getWeightInGrams(), sellOrder3);
        assertLiveOrderRecordOnPosition(1, liveOrderSummary.getSellOrders(), samePriceSell, sellOrder1.getWeightInGrams() + sellOrder2.getWeightInGrams(), sellOrder1, sellOrder2);
    }

    @Test
    void shouldSumWeightOfSamePriceWithDifferrentScale() {
        // given
        BigDecimal samePriceBuy = someBigDecimal().setScale(2, RoundingMode.UP);
        SilverBarOrder buyOrder1 = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, samePriceBuy, someInt());
        SilverBarOrder buyOrder2 = silverBarOrder(someUUID(), someUUID(), OrderType.BUY, samePriceBuy.setScale(3, RoundingMode.UP), someInt());

        BigDecimal samePriceSell = someBigDecimal().setScale(2, RoundingMode.UP);
        SilverBarOrder sellOrder1 = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, samePriceSell, someInt());
        SilverBarOrder sellOrder2 = silverBarOrder(someUUID(), someUUID(), OrderType.SELL, samePriceSell.setScale(3, RoundingMode.UP), someInt());

        when(repository.getAllOrders()).thenReturn(List.of(buyOrder1, sellOrder1, buyOrder2, sellOrder2));

        // when
        LiveOrderSummary liveOrderSummary = service.getLiveOrderSummary();

        // then
        assertThat(liveOrderSummary.getBuyOrders()).hasSize(1);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getBuyOrders(), samePriceBuy, buyOrder1.getWeightInGrams() + buyOrder2.getWeightInGrams(), buyOrder1, buyOrder2);
        assertThat(liveOrderSummary.getSellOrders()).hasSize(1);
        assertLiveOrderRecordOnPosition(0, liveOrderSummary.getSellOrders(), samePriceSell, sellOrder1.getWeightInGrams() + sellOrder2.getWeightInGrams(), sellOrder1, sellOrder2);
    }

    private void assertLiveOrderRecordOnPosition(int position, List<LiveOrderSummaryRecord> orders, BigDecimal price, int grams, SilverBarOrder... originalOrders) {
        assertThat(orders.get(position)).satisfies(liveOrder -> {
            assertThat(liveOrder.getComponents()).hasSize(originalOrders.length).containsExactly(originalOrders);
            assertThat(liveOrder.getThousandGramsPrice()).isEqualByComparingTo(price);
            assertThat(liveOrder.getWeightInGrams()).isEqualTo(grams);
        });
    }
}


