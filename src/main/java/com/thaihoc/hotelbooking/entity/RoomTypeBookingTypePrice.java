package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_type_booking_type_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeBookingTypePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_type_id", nullable = false)
    private BookingType bookingType;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "weekend_surcharge")
    private BigDecimal weekendSurcharge;

    @Column(name = "additional_hour_price")
    private BigDecimal additionalHourPrice;

    @Column(name = "max_hours")
    private Integer maxHours;


}

