package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RoomTypeBookingTypePriceRepository extends JpaRepository<RoomTypeBookingTypePrice, Long> {
    // Tìm tất cả giá theo roomTypeId
    List<RoomTypeBookingTypePrice> findByRoomType_Id(Long roomTypeId);

    // Tìm tất cả giá theo bookingTypeId
    List<RoomTypeBookingTypePrice> findByBookingType_Id(Long bookingTypeId);

    // Kiểm tra xem cặp roomTypeId + bookingTypeId đã tồn tại chưa
    boolean existsByRoomType_IdAndBookingType_Id(Long roomTypeId, Long bookingTypeId);

    Optional<RoomTypeBookingTypePrice> findByRoomType_IdAndBookingType_IdAndIsActive(
            Long roomTypeId,
            Long bookingTypeId,
            Boolean isActive
    );

    @Query("""
        SELECT MIN(r.price)
        FROM RoomTypeBookingTypePrice r
        WHERE r.roomType.id = :roomTypeId
          AND r.isActive = true
    """)
    BigDecimal findMinPriceByRoomTypeId(@Param("roomTypeId") Long roomTypeId);


    @Query("""
        select p
        from RoomTypeBookingTypePrice p
            join fetch p.bookingType bt
            join fetch p.roomType rt
            join fetch rt.branch b
        where p.isActive = true
          and b.status = com.thaihoc.hotelbooking.enums.BranchStatus.ACTIVE
          and (:bookingTypeCode is null or bt.code = :bookingTypeCode)
          and (
                :location is null
                or lower(b.branchName) like lower(concat('%', :location, '%'))
                or lower(b.address) like lower(concat('%', :location, '%'))
          )
    """)
    List<RoomTypeBookingTypePrice> searchCandidates(
            @Param("bookingTypeCode") String bookingTypeCode,
            @Param("location") String location
    );


}
