package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.room.roomType.id = :roomTypeId
          AND b.status IN :statuses
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
    int countActiveBookingsByRoomTypeAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("statuses") List<String> statuses,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );
}
