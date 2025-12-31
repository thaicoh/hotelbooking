package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HotelSearchRequest {
    private String bookingTypeCode;   // HOUR / DAY / NIGHT (optional)
    private LocalDateTime checkIn;    // optional
    private LocalDateTime checkOut;   // optional
    private Integer hours;            // optional (nếu có checkIn nhưng không có checkOut)
    private String location;          // optional
    private BigDecimal minPrice;      // optional
    private BigDecimal maxPrice;      // optional
}
