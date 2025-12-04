package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByTypeName(String typeName);
}
