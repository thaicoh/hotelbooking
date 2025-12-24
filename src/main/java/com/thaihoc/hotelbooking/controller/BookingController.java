package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.BookingListItemResponse;
import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreationRequest request){
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(request))
                .build();
    }


    @GetMapping("/admin/bookings")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ApiResponse<PageResponse<BookingListItemResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String bookingTypeCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPaid,
            @RequestParam(required = false) LocalDate checkInDate
    ) {
        return ApiResponse.<PageResponse<BookingListItemResponse>>builder()
                .result(bookingService.getAllBookings(page, size, search, branchId, roomTypeId, bookingTypeCode, status, isPaid, checkInDate))
                .build();
    }

}
