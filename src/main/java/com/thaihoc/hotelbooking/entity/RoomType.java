package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomType {

    /**
     * Thuộc tính 1: room_type_id
     * - Khóa chính (Primary Key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long id;

    /**
     * Thuộc tính 2: branch_id
     * - Khóa ngoại (Foreign Key) liên kết với Entity Branch (hoặc Location).
     * - Giả định rằng bạn có một Entity tên là Branch.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch; // Thay thế 'Branch' bằng tên Entity thực tế của chi nhánh

    /**
     * Thuộc tính 3: type_name
     * - Tên loại phòng (Ví dụ: "Standard", "Deluxe", "VIP").
     */
    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    /**
     * Thuộc tính 4: capacity
     * - Sức chứa tối đa của loại phòng (số người).
     */
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    /**
     * Thuộc tính 5: description
     * - Mô tả chi tiết loại phòng.
     */
    @Column(name = "description", length = 1000)
    private String description;

}
