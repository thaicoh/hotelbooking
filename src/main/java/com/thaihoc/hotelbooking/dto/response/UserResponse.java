package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String userId;

    private String fullName;

    private String email;

    private String phone;

    private LocalDateTime createdAt;

    private String passwordHash;

    private Set<RoleResponse> roles;
}
