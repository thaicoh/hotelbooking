package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.AdminCreateUserRequest;
import com.thaihoc.hotelbooking.dto.request.UpdateUserStatusRequest;
import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping()
    ApiResponse<UserResponse> create(@Valid @RequestBody UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<UserResponse>> getAll(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getAllPaging(page, size, search, role, status))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> delete( @PathVariable("userId") String userId){
        return ApiResponse.<String>builder()
                .result(userService.delete(userId)?"User has been deleted":"User not found")
                .build();
    }

    @PostMapping("/admin")
    ApiResponse<UserResponse> createByAdmin(@Valid @RequestBody AdminCreateUserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createByAdmin(request))
                .build();
    }

    @PatchMapping("/{email}/status")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable String email,
            @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateStatus(email, request.getStatus()))
                .build();
    }


    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }



}
