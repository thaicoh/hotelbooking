package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.BookingTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BookingTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.BookingTypeResponse;
import com.thaihoc.hotelbooking.service.BookingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking_type")
public class BookingTypeController {

    @Autowired
    private BookingTypeService bookingTypeService;

    @PostMapping
    public ApiResponse<BookingTypeResponse> createBookingType(@RequestBody BookingTypeCreationRequest request) {
        return ApiResponse.<BookingTypeResponse>builder()
                .result(bookingTypeService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BookingTypeResponse>> findAllBookingTypes() {
        return ApiResponse.<List<BookingTypeResponse>>builder()
                .result(bookingTypeService.findAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingTypeResponse> getBookingType(@PathVariable Long id) {
        return ApiResponse.<BookingTypeResponse>builder()
                .result(bookingTypeService.findById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BookingTypeResponse> updateBookingType(@PathVariable Long id,
                                                               @RequestBody BookingTypeUpdateRequest request) {
        return ApiResponse.<BookingTypeResponse>builder()
                .result(bookingTypeService.updateBookingType(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBookingType(@PathVariable Long id) {
        return ApiResponse.<String>builder()
                .result(bookingTypeService.deleteById(id))
                .build();
    }


}
