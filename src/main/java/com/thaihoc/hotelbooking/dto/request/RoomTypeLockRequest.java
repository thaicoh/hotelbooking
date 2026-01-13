package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomTypeLockRequest {
    private Long roomTypeId;
    private Long bookingTypeId;
    private LocalDateTime lockedAt;
    private LocalDateTime unlockedAt;
    private String remarks;
}

