package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupService {

    @Autowired
    private BookingRepository bookingRepository;

    @Scheduled(fixedRate = 300000) // chạy mỗi 5 phút
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expired = bookingRepository
                .findByStatusAndIsPaidFalseAndExpireAtBefore(BookingStatus.PENDING, now);

        for (Booking b : expired) {
            b.setStatus(BookingStatus.CANCELLED);
            b.setCancelledAt(now);
            bookingRepository.save(b);
        }
    }
}

