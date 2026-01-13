package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.enums.BookingStatus;
import lombok.Data;

@Data
public class UpdateBookingStatusRequest {
    private Long bookingId;
    private String status;
}