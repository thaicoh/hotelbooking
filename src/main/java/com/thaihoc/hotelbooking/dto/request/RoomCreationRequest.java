package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.entity.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomCreationRequest {

    private Integer roomNumber;

    private String status; // "Available", "Occupied", "Maintenance"

    private String description;

    private Long roomTypeId;

}
