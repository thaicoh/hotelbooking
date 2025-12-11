package com.thaihoc.hotelbooking.dto.request;

import lombok.Data;

@Data
public class SendOtpRequest {
    private String gmail;
    private String phone;
}
