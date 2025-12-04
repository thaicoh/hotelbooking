package com.thaihoc.hotelbooking.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTypeResponse {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String rules;

    private LocalTime defaultCheckInTime;

    private LocalTime defaultCheckOutTime;

    private Integer durationHours;
}
