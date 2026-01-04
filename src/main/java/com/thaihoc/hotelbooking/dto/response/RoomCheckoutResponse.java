package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RoomCheckoutResponse {
    private Long roomTypeId;
    private String roomTypeName;
    private BigDecimal price;
    private String currency;
    private int availableRooms;
}

