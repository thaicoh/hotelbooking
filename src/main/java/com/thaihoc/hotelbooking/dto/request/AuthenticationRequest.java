package com.thaihoc.hotelbooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticationRequest {

    @NotBlank(message = "EMAIL_REQUIRED")
    private String phoneOrEmail;

    @NotBlank(message = "EMAIL_REQUIRED")
    private String password;
}
