package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    boolean existsByRoomNumber(Integer roomNumber);

    int countByRoomType_Id(Long roomTypeId);

    List<Room> findByRoomTypeId(Long roomTypeId);

    Long countByRoomTypeId(Long roomTypeId);

    boolean existsByRoomNumberAndRoomType(Integer roomNumber, RoomType roomType);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Room r
        WHERE r.roomNumber = :roomNumber
          AND r.roomType.branch = :branch
    """)
    boolean existsByRoomNumberAndBranch(@Param("roomNumber") Integer roomNumber, @Param("branch") Branch branch);
}
