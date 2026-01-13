package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomTypeLockRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeLockResponse;
import com.thaihoc.hotelbooking.entity.RoomTypeLock;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.service.RoomTypeLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room_type_lock")
public class RoomTypeLockController {
    @Autowired
    private RoomTypeLockService lockService;

    @Autowired
    private UserRepository userRepository;

    // Tạo khóa phòng
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_STAFF','SCOPE_ROLE_ADMIN')")
    public ApiResponse<RoomTypeLock> createLock(@RequestBody RoomTypeLockRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ApiResponse.<RoomTypeLock>builder()
                .result(lockService.createLock(
                        request.getRoomTypeId(),
                        request.getBookingTypeId(),
                        user,
                        request.getLockedAt(),
                        request.getUnlockedAt(),
                        request.getRemarks()
                ))
                .build();
    }

    // Xóa khóa phòng
    @DeleteMapping("/{lockId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_STAFF','SCOPE_ROLE_ADMIN')")
    public ApiResponse<String> deleteLock(@PathVariable Long lockId) {
        return ApiResponse.<String>builder()
                .result(lockService.deleteLock(lockId))
                .build();
    }


    // API lấy tất cả khóa phòng theo branch
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_STAFF','SCOPE_ROLE_ADMIN')")
    public ApiResponse<List<RoomTypeLockResponse>> getLocksByBranch(@PathVariable String branchId) {
        return ApiResponse.<List<RoomTypeLockResponse>>builder()
                .result(lockService.getLocksByBranch(branchId))
                .build();
    }

}
