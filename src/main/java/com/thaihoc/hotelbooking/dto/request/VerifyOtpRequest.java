package com.thaihoc.hotelbooking.dto.request;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String gmail;
    private String otp;
}
