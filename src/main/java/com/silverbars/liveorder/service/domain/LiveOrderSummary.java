package com.silverbars.liveorder.service.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class LiveOrderSummary {
    private List<LiveOrderSummaryRecord> buyOrders;
    private List<LiveOrderSummaryRecord> sellOrders;
}
