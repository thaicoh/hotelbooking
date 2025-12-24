package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Lấy tất cả payment theo booking
    List<Payment> findByBooking(Booking booking);

    // Lấy payment mới nhất theo booking (dùng trong BookingService)
    Optional<Payment> findTopByBookingOrderByPaymentDateDesc(Booking booking);

    // Lấy payment theo trạng thái
    List<Payment> findByPaymentStatus(String paymentStatus);

    // Lấy payment theo phương thức
    List<Payment> findByPaymentMethod(String paymentMethod);

    // Lấy payment theo bookingId
    List<Payment> findByBooking_BookingId(Long bookingId);
}
