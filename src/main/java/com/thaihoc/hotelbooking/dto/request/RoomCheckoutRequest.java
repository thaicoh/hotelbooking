package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomCheckoutRequest {
    private Long roomTypeId;       // ID loại phòng
    private String bookingTypeCode;  // HOUR / NIGHT / DAY
    private LocalDateTime checkIn;   // thời gian nhận phòng
    private LocalDateTime checkOut;  // thời gian trả phòng (có thể null nếu theo giờ)
    private Integer hours;           // số giờ (nếu đặt theo giờ)
}
