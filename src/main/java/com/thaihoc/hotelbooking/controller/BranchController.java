package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeSummaryResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import com.thaihoc.hotelbooking.service.BranchService;
import com.thaihoc.hotelbooking.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


}
