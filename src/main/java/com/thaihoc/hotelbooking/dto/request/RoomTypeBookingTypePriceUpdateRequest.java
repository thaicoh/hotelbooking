package com.thaihoc.hotelbooking.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeBookingTypePriceUpdateRequest {

    private Long roomTypeId;        // Cho phép đổi sang RoomType khác (nullable)
    private Long bookingTypeId;     // Cho phép đổi sang BookingType khác (nullable)
    private BigDecimal price;       // Giá mới (nullable)
    private String currency;        // Đơn vị tiền tệ mới (nullable)
    private LocalDate effectiveDate; // Ngày hiệu lực mới (nullable)
    private Boolean isActive;       // Trạng thái hoạt động (nullable)
    private BigDecimal weekendSurcharge; // Phụ phí cuối tuần mới (nullable)
}

