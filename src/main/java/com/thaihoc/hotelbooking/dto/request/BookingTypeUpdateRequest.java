package com.thaihoc.hotelbooking.dto.request;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTypeUpdateRequest {
    /**
     * Mã loại booking (ví dụ: DAY, NIGHT, HOUR).
     */
    private String code;

    /**
     * Tên loại booking (ví dụ: "Theo ngày", "Theo đêm", "Theo giờ").
     */
    private String name;

    /**
     * Mô tả chi tiết loại booking.
     */
    private String description;

    /**
     * Quy tắc áp dụng (ví dụ: chính sách check-in/out).
     */
    private String rules;

    /**
     * Giờ check-in mặc định.
     */
    private LocalTime defaultCheckInTime;

    /**
     * Giờ check-out mặc định.
     */
    private LocalTime defaultCheckOutTime;

    /**
     * Thời lượng (tính bằng giờ) nếu là loại booking theo giờ.
     */
    private Integer durationHours;
}
