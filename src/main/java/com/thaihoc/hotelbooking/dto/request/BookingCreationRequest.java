package com.thaihoc.hotelbooking.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Long roomTypeId;        // Loại phòng (hệ thống sẽ tự chọn room cụ thể)

    private String bookingTypeCode; // NIGHT, DAY, HOURLY

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkInDate;   // Ngày giờ check-in

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkOutDate;  // Ngày giờ check-out

    private Integer numberOfGuests; // Số lượng khách

    private String specialRequests; // Yêu cầu đặc biệt

    private String bookingSource;   // WEB, APP, RECEPTION

    private String paymentMethod;   // PAY_AT_HOTEL hoặc ONLINE
}

