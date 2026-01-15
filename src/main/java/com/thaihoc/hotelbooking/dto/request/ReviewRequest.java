package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ReviewRequest {

    private Long bookingId;
    private Integer rating; // từ 1 đến 5
    private String comment;
}
