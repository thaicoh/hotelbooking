package com.thaihoc.hotelbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomTypeUpdateRequest {

    private String typeName;

    private Integer capacity;

    private String description;

}
