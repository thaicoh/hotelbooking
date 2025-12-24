package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeBookingTypePriceResponse;
import com.thaihoc.hotelbooking.service.RoomTypeBookingTypePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room-type-booking-type-prices")
public class RoomTypeBookingTypePriceController {

    @Autowired
    private RoomTypeBookingTypePriceService priceService;

    // Tạo mới
    @PostMapping
    public ApiResponse<RoomTypeBookingTypePriceResponse> create(@RequestBody RoomTypeBookingTypePriceCreationRequest request) {
        return ApiResponse.<RoomTypeBookingTypePriceResponse>builder()
                .result(priceService.create(request))
                .build();
    }

    // Lấy tất cả
    @GetMapping
    public ApiResponse<List<RoomTypeBookingTypePriceResponse>> findAll() {
        return ApiResponse.<List<RoomTypeBookingTypePriceResponse>>builder()
                .result(priceService.findAll())
                .build();
    }

    // Lấy chi tiết theo id
    @GetMapping("/{id}")
    public ApiResponse<RoomTypeBookingTypePriceResponse> findById(@PathVariable Long id) {
        return ApiResponse.<RoomTypeBookingTypePriceResponse>builder()
                .result(priceService.findById(id))
                .build();
    }

    // Cập nhật
    @PutMapping("/{id}")
    public ApiResponse<RoomTypeBookingTypePriceResponse> update(@PathVariable Long id,
                                                                @RequestBody RoomTypeBookingTypePriceUpdateRequest request) {
        return ApiResponse.<RoomTypeBookingTypePriceResponse>builder()
                .result(priceService.update(id, request))
                .build();
    }

    // Xóa
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        return ApiResponse.<String>builder()
                .result(priceService.deleteById(id))
                .build();
    }

    // Lấy tất cả theo roomTypeId
    @GetMapping("/room-type/{roomTypeId}")
    public ApiResponse<List<RoomTypeBookingTypePriceResponse>> findByRoomTypeId(@PathVariable Long roomTypeId) {
        return ApiResponse.<List<RoomTypeBookingTypePriceResponse>>builder()
                .result(priceService.findByRoomTypeId(roomTypeId))
                .build();
    }

}
