package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomCheckoutRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomCheckoutResponse;
import com.thaihoc.hotelbooking.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired
    private CheckoutService checkoutService;

    @PostMapping("/check-room")
    public ApiResponse<RoomCheckoutResponse> checkRoomAvailability(@RequestBody RoomCheckoutRequest req) {
        return ApiResponse.<RoomCheckoutResponse>builder()
                .result(checkoutService.checkRoomAvailability(
                        req.getRoomTypeId(),
                        req.getBookingTypeCode(),
                        req.getCheckIn(),
                        req.getCheckOut(),
                        req.getHours()
                ))
                .build();
    }
}
