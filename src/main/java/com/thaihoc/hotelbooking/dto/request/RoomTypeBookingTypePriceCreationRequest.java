package com.thaihoc.hotelbooking.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeBookingTypePriceCreationRequest {

    private Long roomTypeId;       // ID của RoomType
    private Long bookingTypeId;    // ID của BookingType
    private BigDecimal price;      // Giá áp dụng
    private String currency;       // Đơn vị tiền tệ (VD: VND, USD)
    private LocalDate effectiveDate; // Ngày bắt đầu hiệu lực
    private Boolean isActive;      // Trạng thái hoạt động
    private BigDecimal weekendSurcharge; // Phụ phí cuối tuần
}

