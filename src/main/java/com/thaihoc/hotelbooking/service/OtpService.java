package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.SendOtpRequest;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailService emailService;
    private final OtpStore otpStore;

    @Autowired
    UserRepository userRepository;

    public void sendOtp(SendOtpRequest request) {

        if(userRepository.existsByEmail(request.getGmail())){
            throw new AppException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        String otp = generateOtp();
        otpStore.saveOtp(request.getGmail(), otp);
        emailService.sendOtp(request.getGmail(), "Mã OTP của bạn là: " + otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpStore.verifyOtp(email, otp);
    }

    public boolean isEmailVerified(String email) {
        return otpStore.isVerified(email);
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}

