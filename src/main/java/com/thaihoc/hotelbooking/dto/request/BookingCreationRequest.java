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

    private Long roomTypeId;        // ID loại phòng

    private String bookingTypeCode; // HOUR / NIGHT / DAY

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkInDate;   // Ngày giờ check-in

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkOutDate;  // Ngày giờ check-out (có thể null nếu theo giờ)

    private Integer hours;           // số giờ (nếu đặt theo giờ)

    private Integer numberOfGuests;  // Số lượng khách

    private String specialRequests;  // Yêu cầu đặc biệt

    private String bookingSource;    // WEB, APP, RECEPTION

    private String paymentMethod;    // PAY_AT_HOTEL hoặc ONLINE
}
