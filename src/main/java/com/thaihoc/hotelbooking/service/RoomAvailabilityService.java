package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import com.thaihoc.hotelbooking.repository.RoomRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class RoomAvailabilityService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public boolean isRoomTypeAvailable(Long roomTypeId, LocalDateTime checkIn, LocalDateTime checkOut) {

        // Tổng số phòng của loại này
        int totalRooms = roomRepository.countByRoomType_Id(roomTypeId);

        // Số booking đã chiếm phòng trong khoảng thời gian
        int occupiedBookings = bookingRepository.countActiveBookingsByRoomTypeAndDateRange(
                roomTypeId,
                List.of("CONFIRMED", "RESERVED", "PENDING"),
                checkIn,
                checkOut
        );

        // ✅ Log chi tiết để kiểm tra
        log.info("Check availability: roomTypeId={}, checkIn={}, checkOut={}, totalRooms={}, occupiedBookings={}",
                roomTypeId, checkIn, checkOut, totalRooms, occupiedBookings);

        return occupiedBookings < totalRooms;

    }

    public int countAvailableRooms(Long roomTypeId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Tổng số phòng của loại này
        int totalRooms = roomRepository.countByRoomType_Id(roomTypeId);

        // Số booking đã chiếm phòng trong khoảng thời gian
        int occupiedBookings = bookingRepository.countActiveBookingsByRoomTypeAndDateRange(
                roomTypeId,
                List.of("CONFIRMED", "RESERVED", "PENDING"),
                checkIn,
                checkOut
        );

        int availableRooms = totalRooms - occupiedBookings;
        log.info("Count available rooms: roomTypeId={}, totalRooms={}, occupiedBookings={}, availableRooms={}",
                roomTypeId, totalRooms, occupiedBookings, availableRooms);

        return Math.max(availableRooms, 0);
    }


}
