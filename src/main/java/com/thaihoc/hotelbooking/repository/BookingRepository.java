package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse;
import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
    SELECT b FROM Booking b
    WHERE (:search IS NULL OR LOWER(b.user.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(b.user.phone) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:branchId IS NULL OR b.roomType.branch.id = :branchId)
      AND (:roomTypeId IS NULL OR b.roomType.id = :roomTypeId)
      AND (:bookingTypeCode IS NULL OR b.bookingType.code = :bookingTypeCode)
      AND (:status IS NULL OR b.status = :status)
      AND (:isPaid IS NULL OR b.isPaid = :isPaid)
      AND (:checkInDate IS NULL OR DATE(b.checkInDate) = :checkInDate)
    """)
    Page<Booking> searchBookings(
            @Param("search") String search,
            @Param("branchId") String branchId,
            @Param("roomTypeId") Long roomTypeId,
            @Param("bookingTypeCode") String bookingTypeCode,
            @Param("status") BookingStatus status,
            @Param("isPaid") Boolean isPaid,
            @Param("checkInDate") LocalDate checkInDate,
            Pageable pageable
    );


    // 1. Thống kê theo NĂM (Liệt kê doanh thu của các năm)
    @Query("SELECT new com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse(" +
            "YEAR(b.checkInDate), SUM(b.totalPrice), COUNT(b)) " +
            "FROM Booking b " +
            "JOIN b.roomType rt " +
            "JOIN rt.branch br " +
            "WHERE b.status <> 'CANCELLED' " +
            "AND (:branchId IS NULL OR br.id = :branchId) " +
            "GROUP BY YEAR(b.checkInDate) " +
            "ORDER BY YEAR(b.checkInDate) DESC")
    List<RevenueStatisticResponse> getRevenueByYear(@Param("branchId") String branchId);

    // 2. Thống kê theo THÁNG (Trong 1 năm cụ thể)
    @Query("SELECT new com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse(" +
            "MONTH(b.checkInDate), SUM(b.totalPrice), COUNT(b)) " +
            "FROM Booking b " +
            "JOIN b.roomType rt " +
            "JOIN rt.branch br " +
            "WHERE b.status <> 'CANCELLED' " +
            "AND YEAR(b.checkInDate) = :year " +
            "AND (:branchId IS NULL OR br.id = :branchId) " +
            "GROUP BY MONTH(b.checkInDate) " +
            "ORDER BY MONTH(b.checkInDate) ASC")
    List<RevenueStatisticResponse> getRevenueByMonth(@Param("year") int year,
                                                     @Param("branchId") String branchId);

    // 3. Thống kê theo NGÀY (Trong 1 tháng, 1 năm cụ thể)
    @Query("SELECT new com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse(" +
            "DAY(b.checkInDate), SUM(b.totalPrice), COUNT(b)) " +
            "FROM Booking b " +
            "JOIN b.roomType rt " +
            "JOIN rt.branch br " +
            "WHERE b.status <> 'CANCELLED' " +
            "AND YEAR(b.checkInDate) = :year " +
            "AND MONTH(b.checkInDate) = :month " +
            "AND (:branchId IS NULL OR br.id = :branchId) " +
            "GROUP BY DAY(b.checkInDate) " +
            "ORDER BY DAY(b.checkInDate) ASC")
    List<RevenueStatisticResponse> getRevenueByDay(@Param("year") int year,
                                                   @Param("month") int month,
                                                   @Param("branchId") String branchId);

    // Lưu ý:
    // - status <> 'CANCELLED': Loại bỏ đơn hủy.
    // - (:branchId IS NULL OR br.id = :branchId): Nếu truyền null sẽ lấy tất cả branch, nếu truyền ID sẽ lọc theo branch đó.

    @Query("""
    SELECT b FROM Booking b
    WHERE b.room IS NOT NULL
      AND b.room.roomType.branch.id = :branchId
      AND :date BETWEEN DATE(b.checkInDate) AND DATE(b.checkOutDate)
      AND b.status NOT IN (:excludedStatuses)
    """)
    List<Booking> findBookingsByBranchAndDate(
            @Param("branchId") String branchId,
            @Param("date") LocalDate date,
            @Param("excludedStatuses") List<BookingStatus> excludedStatuses
    );
}
