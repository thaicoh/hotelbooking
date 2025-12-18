package com.thaihoc.hotelbooking.dto.request;


import com.thaihoc.hotelbooking.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    private UserStatus status;
}
