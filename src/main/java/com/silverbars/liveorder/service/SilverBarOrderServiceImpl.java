package com.silverbars.liveorder.service;

import com.silverbars.liveorder.service.domain.LiveOrderSummary;
import com.silverbars.liveorder.service.domain.LiveOrderSummaryRecord;
import com.silverbars.liveorder.service.domain.OrderType;
import com.silverbars.liveorder.service.domain.SilverBarOrder;
import com.silverbars.liveorder.service.repository.SilverBarOrderRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

class SilverBarOrderServiceImpl implements SilverBarOrderService {

    private SilverBarOrderRepository repository;

    public SilverBarOrderServiceImpl(SilverBarOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public String registerOrder(SilverBarOrder order) {
        validateNewOrder(order);
        repository.saveOrder(generatePersistingObject(order));
        return order.getOrderId();
    }

    private SilverBarOrder generatePersistingObject(SilverBarOrder order) {
        var orderId = UUID.randomUUID().toString();
        return SilverBarOrder.builder()
                .orderId(orderId)
                .orderType(order.getOrderType())
                .thousandGramsPrice(order.getThousandGramsPrice())
                .weightInGrams(order.getWeightInGrams())
                .build();
    }

    private void validateNewOrder(SilverBarOrder order) {
        if (order.getOrderId() != null) {
            throw new IllegalArgumentException("orderId cannot be present for the new record");
        }
        validateNotNull(order.getUserId(), "userId is not present");
        validateNotNull(order.getOrderType(), "orderType is not present");
        validateNotNull(order.getThousandGramsPrice(), "thousandGramsPrice is not present");
    }

    private void validateNotNull(Object fieldValue, String validationMessage) {
        if (fieldValue == null) {
            throw new IllegalArgumentException(validationMessage);
        }
    }

    @Override
    public void cancelRegisteredOrder(String orderId) {
        repository.cancelOrder(orderId);
    }

    @Override
    public LiveOrderSummary getLiveOrderSummary() {
        Map<OrderType, List<SilverBarOrder>> ordersByOrderType = repository.getAllOrders().stream()
                .collect(Collectors.groupingBy(SilverBarOrder::getOrderType));

        return LiveOrderSummary.builder()
                .buyOrders(getBuyRecords(ordersByOrderType))
                .sellOrders(getSellRecords(ordersByOrderType)).build();
    }

    private List<LiveOrderSummaryRecord> getSellRecords(Map<OrderType, List<SilverBarOrder>> ordersByOrderType) {
        var sellOrders = processOrderGroup(ordersByOrderType.getOrDefault(OrderType.SELL, Collections.emptyList()));
        sellOrders.sort(Comparator.comparing(LiveOrderSummaryRecord::getThousandGramsPrice).reversed());
        return sellOrders;
    }

    private List<LiveOrderSummaryRecord> getBuyRecords(Map<OrderType, List<SilverBarOrder>> byOrderType) {
        var buyOrders = processOrderGroup(byOrderType.getOrDefault(OrderType.BUY, Collections.emptyList()));
        buyOrders.sort(Comparator.comparing(LiveOrderSummaryRecord::getThousandGramsPrice));
        return buyOrders;
    }

    private List<LiveOrderSummaryRecord> processOrderGroup(List<SilverBarOrder> orderGroup) {
        return orderGroup.stream()
                .reduce(new HashMap<>(), this::addToSameValueBucketIgnoringScale, this::sameMapCombiner)
                .entrySet()
                .stream()
                .map(this::toOrderSummaryRecord)
                .collect(Collectors.toList());
    }

    private HashMap<BigDecimal, List<SilverBarOrder>> addToSameValueBucketIgnoringScale(HashMap<BigDecimal, List<SilverBarOrder>> ordersByPrice, SilverBarOrder order) {
        var samePriceIgnoringScale = ordersByPrice.keySet().stream()
                .filter(existingPrice -> existingPrice.compareTo(order.getThousandGramsPrice()) == 0)
                .findFirst()
                .orElse(order.getThousandGramsPrice());
        ordersByPrice.computeIfAbsent(samePriceIgnoringScale, price -> new ArrayList<>())
                .add(order);
        return ordersByPrice;
    }

    private HashMap<BigDecimal, List<SilverBarOrder>> sameMapCombiner(HashMap<BigDecimal, List<SilverBarOrder>> m1, HashMap<BigDecimal, List<SilverBarOrder>> m2) {
        return m1;
    }

    private LiveOrderSummaryRecord toOrderSummaryRecord(Map.Entry<BigDecimal, List<SilverBarOrder>> ordersByPrice) {
        return LiveOrderSummaryRecord.builder()
                .components(ordersByPrice.getValue())
                .thousandGramsPrice(ordersByPrice.getKey())
                .weightInGrams(sumWeightInGrams(ordersByPrice.getValue()))
                .build();
    }

    private int sumWeightInGrams(List<SilverBarOrder> orders) {
        return orders.stream()
                .map(SilverBarOrder::getWeightInGrams)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
