package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"booking_id"})
        })
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    /**
     * Người viết review
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Review gắn với booking (1 booking chỉ được review 1 lần)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Số sao (1–5)
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Nội dung review
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;


    /**
     * Thời gian tạo
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian chỉnh sửa
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
