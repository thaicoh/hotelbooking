package com.thaihoc.hotelbooking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeBookingTypePriceResponse {

    private Long id;               // Khóa chính
    private Long roomTypeId;       // ID của RoomType
    private Long bookingTypeId;    // ID của BookingType
    private BigDecimal price;      // Giá áp dụng
    private String currency;       // Đơn vị tiền tệ
    private LocalDate effectiveDate; // Ngày bắt đầu hiệu lực
    private Boolean isActive;      // Trạng thái hoạt động
    private BigDecimal weekendSurcharge; // Phụ phí cuối tuần
}
