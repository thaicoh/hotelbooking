package com.thaihoc.hotelbooking.dto.response;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingListItemResponse {
    private Long bookingId;
    private String bookingReference;

    private String customerName;
    private String customerPhone;

    private String branchName;
    private String roomTypeName;
    private String bookingTypeName;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    private BigDecimal totalPrice;
    private String currency;

    private String status;
    private Boolean isPaid;
    private String paymentStatus;

    private LocalDateTime createdAt;

    private String roomId;
    private Integer roomNumber;

    private String bookingTypeCode;

    private Boolean reviewed;   // đã được đánh giá chưa
    private Integer rating;     // số sao đánh giá (1–5)

}

