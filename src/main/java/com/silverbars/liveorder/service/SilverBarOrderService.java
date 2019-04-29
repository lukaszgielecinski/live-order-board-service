package com.silverbars.liveorder.service;

import com.silverbars.liveorder.service.domain.LiveOrderSummary;
import com.silverbars.liveorder.service.domain.SilverBarOrder;

public interface SilverBarOrderService {

    /**
     * Register new order
     *
     * @param order an object representing the order to register, all fields except orderId are required.
     * @return orderId
     */
    String registerOrder(SilverBarOrder order);

    /**
     * Cancel order with given orderId
     *
     * @param orderId required parameter with order id, expected in UUID format.
     */
    void cancelRegisteredOrder(String orderId);

    /**
     * Retrieve all orders grouped by orderType and sorted by price.
     * @return LiveOrderSummary aggregate object
     */
    LiveOrderSummary getLiveOrderSummary();
}
