package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionPreference;
    private LocalDateTime paymentDate;
    private String notes;
}

