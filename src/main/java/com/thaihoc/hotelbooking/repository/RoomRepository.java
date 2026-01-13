package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    boolean existsByRoomNumber(Integer roomNumber);

    int countByRoomType_Id(Long roomTypeId);

    List<Room> findByRoomTypeIdOrderByRoomNumberDesc(Long roomTypeId);

    Long countByRoomTypeId(Long roomTypeId);

    boolean existsByRoomNumberAndRoomType(Integer roomNumber, RoomType roomType);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Room r
        WHERE r.roomNumber = :roomNumber
          AND r.roomType.branch = :branch
    """)
    boolean existsByRoomNumberAndBranch(@Param("roomNumber") Integer roomNumber, @Param("branch") Branch branch);



    @Query("""
    SELECT r FROM Room r
    WHERE r.roomType.id = :roomTypeId
      AND NOT EXISTS (
          SELECT 1 FROM Booking b
          WHERE b.room = r
            AND b.status IN :statuses
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
      )
    ORDER BY r.roomNumber DESC
    """)
    List<Room> findAvailableRoomsByRoomTypeAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );


    List<Room> findByRoomTypeBranchIdOrderByRoomNumberDesc(String branchId);


}
