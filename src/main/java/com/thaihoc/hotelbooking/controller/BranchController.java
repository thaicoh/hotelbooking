package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.request.HotelDetailRequest;
import com.thaihoc.hotelbooking.dto.request.HotelSearchRequest;
import com.thaihoc.hotelbooking.dto.response.*;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import com.thaihoc.hotelbooking.enums.BranchStatus;
import com.thaihoc.hotelbooking.service.BranchService;
import com.thaihoc.hotelbooking.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/branch")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @Autowired
    private RoomTypeService roomTypeService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BranchResponse> createBranch(
            // 1. Nhận DTO (dữ liệu JSON)
            @RequestPart("branchRequest") BranchCreationRequest request,

            // 2. Nhận File ảnh (dữ liệu binary)
            @RequestPart("photo") MultipartFile photo
    ) {

        return  ApiResponse.<BranchResponse>builder()
                .result(branchService.create(request, photo))
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBranch(@PathVariable String id){
        return ApiResponse.<String>builder()
                .result(branchService.delete(id))
                .build();
    }


    @GetMapping
    public ApiResponse<List<Branch>> getAllBranches(){
        return ApiResponse.<List<Branch>>builder()
                .result(branchService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BranchResponse> updateBranch(
            @PathVariable String id,
            @RequestPart("branchRequest") BranchUpdateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo){

        return ApiResponse.<BranchResponse>builder()
                .result(branchService.update(id, request, photo))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BranchResponse> getBranchById(@PathVariable String id){
        return ApiResponse.<BranchResponse>builder()
                .result(branchService.getById(id))
                .build();
    }

    // Phan trang và tìm kiếm theo tên và địa chỉ
    @GetMapping("/paging")
    public ApiResponse<PageResponse<BranchResponse>> getAllPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.<PageResponse<BranchResponse>>builder()
                .result(branchService.getAllPaging(page, size, search))
                .build();
    }


    @GetMapping("/{branchId}/room-types")
    public ApiResponse<List<RoomTypeSummaryResponse>> getRoomTypesByBranch(@PathVariable String branchId) {
        return ApiResponse.<List<RoomTypeSummaryResponse>>builder()
                .result(roomTypeService.getSummaryByBranch(branchId))
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<BranchResponse> updateBranchStatus(
            @PathVariable String id,
            @RequestParam BranchStatus status // truyền trực tiếp enum qua query param hoặc body
    ) {
        return ApiResponse.<BranchResponse>builder()
                .result(branchService.updateStatus(id, status))
                .build();
    }

    @GetMapping("/search-hotels")
    public ApiResponse<List<BranchMinPriceResponse>> searchHotels(
            @RequestParam(required = false) String bookingTypeCode,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime checkIn,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime checkOut,

            @RequestParam(required = false) Integer hours,

            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal   minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ApiResponse.<List<BranchMinPriceResponse>>builder()
                .result(branchService.searchHotels(
                        bookingTypeCode, checkIn, checkOut, hours, location, minPrice, maxPrice
                ))
                .build();
    }

    @PostMapping("/search-hotels")
    public ApiResponse<List<BranchMinPriceResponse>> searchHotels(@RequestBody HotelSearchRequest req) {
        return ApiResponse.<List<BranchMinPriceResponse>>builder()
                .result(branchService.searchHotels(
                        req.getBookingTypeCode(),

                        req.getCheckIn(),
                        req.getCheckOut(),
                        req.getHours(),
                        req.getLocation(),
                        req.getMinPrice(),
                        req.getMaxPrice()
                ))
                .build();
    }


    @PostMapping("/{branchId}/hotel-detail")
    public ApiResponse<BranchDetailResponse> getHotelDetail(
            @PathVariable String branchId,
            @RequestBody HotelDetailRequest req
    ) {
        return ApiResponse.<BranchDetailResponse>builder()
                .result(branchService.getHotelDetail(
                        branchId,
                        req.getBookingTypeCode(),
                        req.getCheckIn(),
                        req.getCheckOut(),
                        req.getHours()
                ))
                .build();
    }


}
