package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Branch {/**
     * Thuộc tính 1: branch_id
     * - Khóa chính (Primary Key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id")
    private String id;

    /**
     * Thuộc tính 2: branch_name
     */
    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    /**
     * Thuộc tính 3: address
     */
    @Column(name = "address", length = 300)
    private String address;

    /**
     * Thuộc tính 4: phone
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Thuộc tính 5: email
     */
    @Column(name = "email", length = 100, unique = true)
    private String email;

    /**
     * Thuộc tính 6: created_at
     * - Lưu trữ thời điểm bản ghi được tạo.
     * - LocalDateTime là kiểu dữ liệu chuẩn cho ngày giờ trong Java.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thuộc tính 7: photo
     * - Lưu URL hoặc đường dẫn đến ảnh đại diện của chi nhánh.
     */
    @Column(name = "photo", length = 255)
    private String photoUrl;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
