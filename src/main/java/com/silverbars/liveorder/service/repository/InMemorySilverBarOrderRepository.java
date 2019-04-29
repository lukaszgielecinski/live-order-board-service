package com.silverbars.liveorder.service.repository;

import com.silverbars.liveorder.service.domain.SilverBarOrder;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class InMemorySilverBarOrderRepository implements SilverBarOrderRepository {

    private HashSet<SilverBarOrder> orders = new HashSet<>();

    @Override
    public void saveOrder(SilverBarOrder order) {
        orders.add(order);
    }

    @Override
    public void cancelOrder(String orderId) {
        orders.removeIf(orderWithGivenId(orderId));
    }

    private Predicate<SilverBarOrder> orderWithGivenId(String orderId) {
        return order -> order.getOrderId().equals(orderId);
    }

    @Override
    public Collection<SilverBarOrder> getAllOrders() {
        return orders;
    }
}
