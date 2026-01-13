package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final RevenueService revenueService;

    private final UserRepository userRepository;


    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF')")
    public ApiResponse<List<RevenueStatisticResponse>> getRevenue(
            @RequestParam(defaultValue = "YEARLY") String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String branchId
    ) {
        // 👉 Lấy user hiện tại từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 👉 Nếu là STAFF thì override branchId
        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("STAFF"));
        if (isStaff) {
            if (user.getBranch() == null) {
                throw new AppException(ErrorCode.BRANCH_NOT_FOUND, "staff branch not found");
            }
            branchId = user.getBranch().getId();
        } else {
            // Chuẩn hóa chuỗi rỗng thành null
            if (branchId != null && branchId.trim().isEmpty()) {
                branchId = null;
            }
        }

        return ApiResponse.<List<RevenueStatisticResponse>>builder()
                .code(1000)
                .message("Success")
                .result(revenueService.getRevenueStatistics(type, year, month, branchId))
                .build();
    }

}