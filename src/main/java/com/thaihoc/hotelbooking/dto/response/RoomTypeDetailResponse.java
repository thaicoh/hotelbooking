package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RoomTypeDetailResponse {
    private Long roomTypeId;
    private String roomTypeName;
    private Integer capacity;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer availableRooms; // số lượng phòng còn trống


    private List<String> photoUrls; // danh sách link ảnh của roomtype
}
