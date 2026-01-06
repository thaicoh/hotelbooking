package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RoomCheckoutResponse {
    private RoomTypeResponse roomType;
    private BranchResponse branch;

    private String bookingTypeCode;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer hours;

    private BigDecimal price;
    private String currency;
    private int availableRooms;


    // Thông tin user từ token
    private UserResponse user;


}

