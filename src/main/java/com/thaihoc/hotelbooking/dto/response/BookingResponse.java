package com.thaihoc.hotelbooking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;

    private String userId;
    private Long roomTypeId;
    private Long bookingTypeId;
    private String roomId;

    private BigDecimal totalPrice;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    private String bookingSource;
    private Boolean isPaid;
    private LocalDateTime cancelledAt;

    private String bookingReference;
    private String createdBy;

    private String specialRequests;
    private Integer numberOfGuests;
}

