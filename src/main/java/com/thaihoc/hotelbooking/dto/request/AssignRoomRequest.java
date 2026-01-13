package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

@Data
public class AssignRoomRequest {
    private Long bookingId;
    private String roomId;
}
