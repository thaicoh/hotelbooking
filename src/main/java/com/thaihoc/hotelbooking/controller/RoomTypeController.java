package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeResponse;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.service.RoomTypeService;
import jakarta.persistence.MappedSuperclass;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room_type")
public class RoomTypeController {
    @Autowired
    RoomTypeService roomTypeService;

    @PostMapping
    private ApiResponse<RoomTypeResponse> createRoomType(@RequestBody RoomTypeCreationRequest request) {
        return ApiResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.create(request))
                .build();
    }

    @GetMapping
    private ApiResponse<List<RoomTypeResponse>> findAllRoomTypes(){
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .result(roomTypeService.findAll())
                .build();
    }

    @DeleteMapping("/{id}")
    private ApiResponse<String> deleteRoomType(@PathVariable Long id){
        return ApiResponse.<String>builder()
                .result(roomTypeService.deleteById(id))
                .build();
    }

    @PutMapping("/{id}")
    private ApiResponse<RoomTypeResponse> updateRoomType(@RequestBody RoomTypeUpdateRequest request, @PathVariable Long id) {
        return ApiResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.updateRoomType(id, request))
                .build();
    }


    @GetMapping("/{id}")
    private ApiResponse<RoomTypeResponse> getRoomType(@PathVariable Long id){
        return ApiResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.findById(id))
                .build();
    }

    @GetMapping("/branch/{branchId}")
    private ApiResponse<List<RoomTypeResponse>> getRoomTypesByBranch(@PathVariable String branchId) {
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .result(roomTypeService.findByBranchId(branchId))
                .build();
    }
}
