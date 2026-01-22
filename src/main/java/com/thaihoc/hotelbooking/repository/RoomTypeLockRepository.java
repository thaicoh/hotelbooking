package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.RoomTypeLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomTypeLockRepository extends JpaRepository<RoomTypeLock, Long> {
    List<RoomTypeLock> findByRoomTypeId(Long roomTypeId);

    List<RoomTypeLock> findByRoomTypeBranchId(String branchId);

    @Query("""
        SELECT COUNT(l) FROM RoomTypeLock l
        WHERE l.roomType.id = :roomTypeId
          AND l.roomType.branch.id = :branchId
          AND l.bookingType.code = :bookingTypeCode
          AND l.lockedAt <= :checkOut
          AND (l.unlockedAt IS NULL OR l.unlockedAt >= :checkIn)
    """)
    int countLocksByRoomTypeAndBranchAndBookingTypeAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("branchId") String branchId,
            @Param("bookingTypeCode") String bookingTypeCode,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );
}
