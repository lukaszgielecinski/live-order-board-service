package com.silverbars.liveorder.service.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class LiveOrderSummaryRecord {

    private BigDecimal thousandGramsPrice;
    private int weightInGrams;

    private List<SilverBarOrder> components;
}
