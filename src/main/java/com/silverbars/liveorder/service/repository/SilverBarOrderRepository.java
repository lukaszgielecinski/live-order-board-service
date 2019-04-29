package com.silverbars.liveorder.service.repository;

import com.silverbars.liveorder.service.domain.SilverBarOrder;

import java.util.Collection;

public interface SilverBarOrderRepository {

    void saveOrder(SilverBarOrder order);

    void cancelOrder(String orderId);

    Collection<SilverBarOrder> getAllOrders();
}
