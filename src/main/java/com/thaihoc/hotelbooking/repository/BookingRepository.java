package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    SELECT COUNT(b) FROM Booking b
    WHERE b.roomType.id = :roomTypeId
      AND b.status IN :statuses
      AND b.checkInDate < :checkOut
      AND b.checkOutDate > :checkIn
""")
    int countActiveBookingsByRoomTypeAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );



    Optional<Booking> findByBookingReference(String bookingReference);

    // hàm tìm booking hết hạn chưa thanh toán
    List<Booking> findByStatusAndIsPaidFalseAndExpireAtBefore(BookingStatus status, LocalDateTime now);

    // tìm theo nhiều trạng thái
    List<Booking> findByStatusInAndIsPaidFalseAndExpireAtBefore(List<String> statuses, LocalDateTime now);


    // Lấy tất cả booking theo user
    List<Booking> findByUser(User user);

    // lấy theo userId
    List<Booking> findByUser_UserId(String userId);

    // sắp xếp theo ngày tạo
    List<Booking> findByUserOrderByCreatedAtDesc(User user);


}
