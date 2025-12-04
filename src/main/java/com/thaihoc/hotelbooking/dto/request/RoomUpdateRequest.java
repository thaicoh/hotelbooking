package com.thaihoc.hotelbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomUpdateRequest {

    private Integer roomNumber;

    private String status; // "Available", "Occupied", "Maintenance"

    private String description;

    private Long roomTypeId;
}
