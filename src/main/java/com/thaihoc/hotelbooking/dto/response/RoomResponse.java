package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponse {

    private String roomId;

    private Integer roomNumber;

    private String status; // "Available", "Occupied", "Maintenance"

    private String description;

    private RoomTypeResponse roomType;

}
