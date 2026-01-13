package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponse {
    private List<RoomResponse> allRooms;
    private List<RoomResponse> availableRooms;
}
