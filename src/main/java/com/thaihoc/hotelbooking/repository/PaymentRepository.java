package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Payment;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Lấy payment theo bookingId
    List<Payment> findByBooking_BookingId(Long bookingId);

    // Lấy payment mới nhất theo booking (dùng trong BookingService)
    Optional<Payment> findTopByBookingOrderByPaymentDateDesc(Booking booking);

    List<Payment> findByBooking(Booking booking);

}
