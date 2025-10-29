package com.thaihoc.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String phone;

    private LocalDateTime createdAt;

    @ManyToMany
    private Set<Role> roles;
}
