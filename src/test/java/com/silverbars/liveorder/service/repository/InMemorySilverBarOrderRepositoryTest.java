package com.silverbars.liveorder.service.repository;

import com.silverbars.liveorder.service.domain.SilverBarOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.silverbars.liveorder.service.TestFixtures.someSilverBarOrder;
import static org.assertj.core.api.Assertions.assertThat;

class InMemorySilverBarOrderRepositoryTest {

    private SilverBarOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySilverBarOrderRepository();
    }

    @Test
    void shouldSaveOrder() {
        // given
        var order = someSilverBarOrder();

        // when
        repository.saveOrder(order);

        // then
        assertThat(repository.getAllOrders())
                .hasSize(1)
                .contains(order);
    }

    @Test
    void shouldSaveManyOrders() {
        // given
        var order1 = someSilverBarOrder();
        var order2 = someSilverBarOrder();
        var order3 = someSilverBarOrder();

        // when
        repository.saveOrder(order1);
        repository.saveOrder(order2);
        repository.saveOrder(order3);

        // then
        assertThat(repository.getAllOrders())
                .hasSize(3)
                .containsExactlyInAnyOrder(order1, order2, order3);
    }

    @Test
    void shouldCancelOrder() {
        // given
        var order1 = saveOrder();
        var order2 = saveOrder();
        var order3 = saveOrder();

        // when
        repository.cancelOrder(order2.getOrderId());

        // then
        assertThat(repository.getAllOrders())
                .hasSize(2)
                .containsExactlyInAnyOrder(order1, order3);
    }

    private SilverBarOrder saveOrder() {
        var order = someSilverBarOrder();
        repository.saveOrder(order);
        return order;
    }
}