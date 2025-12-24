package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.RoomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Long> {
    void deleteByRoomTypeId(Long roomTypeId);

    boolean existsByRoomTypeId(Long roomTypeId);

    List<RoomPhoto> findByRoomTypeId(Long roomTypeId);

    Optional<RoomPhoto> findByRoomTypeIdAndIsMainTrue(Long roomTypeId);

    Long countByRoomTypeId(Long roomTypeId);

    boolean existsByRoomTypeIdAndIsMainTrue(Long roomTypeId);


}
