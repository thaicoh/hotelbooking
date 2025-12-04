package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID riêng của bức ảnh

    /**
     * URL / Đường dẫn của ảnh (ảnh được lưu ngoài DB).
     */
    @Column(name = "photo_url", nullable = false, length = 255)
    private String photoUrl;

    /**
     * Mặc định là false. True nếu đây là ảnh đại diện chính.
     */
    @Column(name = "is_main")
    private Boolean isMain = false;

    /**
     * KHÓA NGOẠI: Mối quan hệ Many-to-One.
     * Cột khóa ngoại 'room_type_id' sẽ được tạo trong bảng RoomPhoto.
     */
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;
}
