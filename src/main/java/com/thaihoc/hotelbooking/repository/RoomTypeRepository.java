package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByTypeName(String typeName);

    List<RoomType> findByBranchId(String branchId);
    boolean existsByTypeNameAndBranchId(String typeName, String branchId);

}
