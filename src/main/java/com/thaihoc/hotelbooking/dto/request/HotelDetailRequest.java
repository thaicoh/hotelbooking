package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HotelDetailRequest {
    private String bookingTypeCode;   // loại đặt phòng: HOUR / DAY / NIGHT
    private LocalDateTime checkIn;    // thời gian check-in
    private LocalDateTime checkOut;   // thời gian check-out
    private Integer hours;            // số giờ (nếu bookingTypeCode = HOUR)
}
