package com.thaihoc.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomTypeSummaryResponse {
    private Long id;
    private String typeName;
    private Integer capacity;

    private String mainPhotoUrl; // ảnh đại diện
    private Long roomCount;      // số phòng thuộc loại này
    private BigDecimal minPrice; // giá thấp nhất

}
