package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {

    private Long reviewId;

    private UserResponse user;

    private BookingResponse booking;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
