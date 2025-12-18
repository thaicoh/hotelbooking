package com.thaihoc.hotelbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendOtpRequest {

    // Gmail (email)
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String gmail;

    // Phone (số điện thoại)
    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(
            regexp = "^(0\\d{9}|\\+?\\d{10,15})$",
            message = "PHONE_INVALID"
    )
    private String phone;

    // Password (tối thiểu 6 ký tự)
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    private String password;

    // Name (tên người dùng)
    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(min = 3, max = 50, message = "FULL_NAME_REQUIRED")
    private String fullName;

}
