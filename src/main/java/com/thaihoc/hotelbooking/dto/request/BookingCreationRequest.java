package com.thaihoc.hotelbooking.dto.request;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreationRequest {

    private Long userId;            // ID của khách hàng
    private Long roomTypeId;        // Loại phòng
    private Long bookingTypeId;     // Loại booking (theo ngày, theo giờ...)
    private String roomId;            // Phòng cụ thể (nếu chọn trước)

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    private Integer numberOfGuests; // Số lượng khách
    private String specialRequests; // Yêu cầu đặc biệt

    private String bookingSource;   // Nguồn booking (web, app, lễ tân...)
    private String paymentMethod; // "PAY_AT_HOTEL" hoặc "ONLINE"
}
