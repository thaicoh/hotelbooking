package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_lock_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType; // Loại phòng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_type_id", nullable = false)
    private BookingType bookingType; // Loại đặt phòng

    @Column(name = "locked_by", length = 100)
    private String lockedBy; // Người thực hiện khóa (có thể là tên nhân viên)

    @Column(name = "locked_at")
    private LocalDateTime lockedAt; // Thời gian khóa phòng

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt; // Thời gian mở khóa (nếu có)

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks; // Ghi chú về lý do khóa hoặc các chi tiết khác
}
