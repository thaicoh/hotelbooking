package com.thaihoc.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticResponse {
    private Integer timeUnit; // Đại diện cho Năm (2025), Tháng (1-12), hoặc Ngày (1-31)
    private BigDecimal totalRevenue; // Tổng tiền
    private Long bookingCount; // Số lượng đơn
}