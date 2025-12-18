package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    boolean existsByRoomNumber(Integer roomNumber);

    int countByRoomType_Id(Long roomTypeId);

    List<Room> findByRoomTypeId(Long roomTypeId);

}
