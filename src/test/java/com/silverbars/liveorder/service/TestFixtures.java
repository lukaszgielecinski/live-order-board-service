package com.silverbars.liveorder.service;

import com.silverbars.liveorder.service.domain.OrderType;
import com.silverbars.liveorder.service.domain.SilverBarOrder;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestFixtures {

    static int someInt() {
        return ThreadLocalRandom.current().nextInt(100_000);
    }

    static String someUUID() {
        return UUID.randomUUID().toString();
    }

    static BigDecimal someBigDecimal() {
        return new BigDecimal(Math.random());
    }

    static String someString() {
        var stringLength = ThreadLocalRandom.current().nextInt(10);
        return RandomStringUtils.random(stringLength);
    }

    static OrderType someOrderType() {
        var randomElementIndex = ThreadLocalRandom.current().nextInt(OrderType.values().length);
        return OrderType.values()[randomElementIndex];
    }

    public static SilverBarOrder someSilverBarOrder() {
        return someSilverBarOrder(someUUID());
    }

    static SilverBarOrder someNewSilverBarOrder() {
        return someSilverBarOrder(null);
    }

    private static SilverBarOrder someSilverBarOrder(String orderId) {
        return silverBarOrder(orderId, someString(), someOrderType(), someBigDecimal(), someInt());
    }

    static SilverBarOrder silverBarOrder(String orderId, String userId, OrderType orderType, BigDecimal thousandGramsPrice, int weightInGrams) {
        return SilverBarOrder.builder()
                .orderId(orderId)
                .userId(userId)
                .orderType(orderType)
                .thousandGramsPrice(thousandGramsPrice)
                .weightInGrams(weightInGrams)
                .build();
    }
}
