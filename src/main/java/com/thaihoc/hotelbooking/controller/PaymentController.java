package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Payment;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import com.thaihoc.hotelbooking.repository.PaymentRepository;
import com.thaihoc.hotelbooking.service.PaymentService;
import com.thaihoc.hotelbooking.service.VnPayService;
import com.thaihoc.hotelbooking.util.VnPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/vnpay-callback")
    public ApiResponse<?> handleVnPayCallback(@RequestBody Map<String, String> params) {
        boolean success = paymentService.processVnPayCallback(params);

        if (success) {
            return ApiResponse.builder()
                    .code(1000)
                    .message("Payment success")
                    .result(null)
                    .build();
        } else {
            return ApiResponse.builder()
                    .code(2001)
                    .message("Payment failed or invalid signature")
                    .result(null)
                    .build();
        }
    }
}

