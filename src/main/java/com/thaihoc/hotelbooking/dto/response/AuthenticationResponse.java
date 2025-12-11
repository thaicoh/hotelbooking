package com.thaihoc.hotelbooking.dto.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthenticationResponse {
    private boolean authenticated;
    private String token;
    private UserResponse  user;
}
