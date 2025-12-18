package com.thaihoc.hotelbooking.controller;

import com.nimbusds.jose.JOSEException;
import com.thaihoc.hotelbooking.dto.request.*;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.AuthenticationResponse;
import com.thaihoc.hotelbooking.dto.response.IntrospectResponse;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.service.AuthenticationService;
import com.thaihoc.hotelbooking.service.OtpService;
import jakarta.validation.Valid;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    OtpService otpService;

    @PostMapping()
    ApiResponse<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout( @RequestBody LogoutRequest request) throws ParseException, JOSEException {

        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> logout( @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {

        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }




    // ✅ 1. Gửi OTP
    @PostMapping("/send-otp")
    public ApiResponse<String> sendOtp(@RequestBody @Valid SendOtpRequest request) {
        otpService.sendOtp(request);
        return ApiResponse.<String>builder()
                .result("OTP sent")
                .build();
    }

    // ✅ 2. Xác thực OTP
    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getGmail(), request.getOtp());

        if (!isValid) {
            throw  new AppException(ErrorCode.INVALID_OTP);
        }

        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }


}
