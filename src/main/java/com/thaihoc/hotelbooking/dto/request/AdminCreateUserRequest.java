package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AdminCreateUserRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String email;

    private String phone;

    @NotBlank
    private String password;

    @NotEmpty
    private Set<String> roles;   // ví dụ: ["ADMIN", "STAFF", "USER"]

    private String branchId;     // nullable nếu không phải STAFF

    // ⭐ Trạng thái người dùng (ACTIVE, LOGIN_LOCKED, BOOKING_LOCKED)
    private UserStatus status;

}

