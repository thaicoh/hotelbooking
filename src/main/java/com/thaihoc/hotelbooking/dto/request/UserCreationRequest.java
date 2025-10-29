package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {

    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(min = 3, message = "FULL_NAME_INVALID")
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_TOO_SHORT")
    private String password;

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "PHONE_INVALID")
    private String phone;

    private LocalDateTime createdAt;
}
