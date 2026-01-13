package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {
    private final UserRepository userRepository;
    private final BranchMapper branchMapper;

    @GetMapping("/my-branch")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STAFF')")
    public ApiResponse<BranchResponse> getMyBranch() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getBranch() == null) {
            throw new AppException(ErrorCode.BRANCH_NOT_FOUND, "staff branch not found");
        }

        Branch branch = user.getBranch();

        BranchResponse res = branchMapper.toBranchResponse(branch);

        return ApiResponse.<BranchResponse>builder()
                .code(1000)
                .message("Success")
                .result(res)
                .build();
    }

}
