package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBooking(Booking booking);

    // Lấy review theo branchId
    @Query("SELECT r FROM Review r WHERE r.booking.roomType.branch.id = :branchId")
    List<Review> findAllByBranchId(@Param("branchId") String branchId);

    // Lấy review theo roomTypeId
    @Query("SELECT r FROM Review r WHERE r.booking.roomType.id = :roomTypeId")
    List<Review> findAllByRoomTypeId(@Param("roomTypeId") Long roomTypeId);


    // ✅ Lấy review trực tiếp theo bookingId
    Optional<Review> findByBooking_BookingId(Long bookingId);

    // ✅ Kiểm tra có review nào ứng với bookingId hay không
    boolean existsByBooking_BookingId(Long bookingId);

    @Query("SELECT r FROM Review r WHERE r.booking.roomType.branch.id = :branchId")
    Page<Review> findAllByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.booking.roomType.branch.id = :branchId AND r.booking.roomType.id = :roomTypeId")
    List<Review> findAllByBranchIdAndRoomTypeId(@Param("branchId") String branchId,
                                                @Param("roomTypeId") Long roomTypeId);
}
