package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeLockResponse {@Id

    private Long id;

    private RoomTypeResponse roomType;

    private BookingTypeResponse bookingType;

    private String lockedBy; // Người thực hiện khóa

    private LocalDateTime lockedAt; // Thời gian khóa

    private LocalDateTime unlockedAt; // Thời gian mở khóa

    private String remarks; // Ghi chú

}
