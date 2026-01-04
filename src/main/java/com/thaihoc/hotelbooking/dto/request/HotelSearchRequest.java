package com.thaihoc.hotelbooking.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HotelSearchRequest {
    private String bookingTypeCode;   // HOUR / DAY / NIGHT (optional)

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkIn;    // ví dụ: 2026-01-02T11:00:00

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkOut;   // ví dụ: 2026-01-02T15:00:00

    private Integer hours;            // optional (nếu có checkIn nhưng không có checkOut)
    private String location;          // optional
    private BigDecimal minPrice;      // optional
    private BigDecimal maxPrice;      // optional
}
