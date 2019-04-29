package com.silverbars.liveorder.service.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@EqualsAndHashCode
public class SilverBarOrder {

    private String orderId;
    private String userId;
    private OrderType orderType;
    private BigDecimal thousandGramsPrice;
    private int weightInGrams;
}
