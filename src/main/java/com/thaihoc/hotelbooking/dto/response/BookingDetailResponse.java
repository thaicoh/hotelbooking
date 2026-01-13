package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingDetailResponse {
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
    private LocalDateTime createdAt;
    private String roomId;
    private Integer roomNumber;
    private List<PaymentResponse> payments;
}

