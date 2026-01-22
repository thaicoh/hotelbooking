package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.ReviewRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.ReviewResponse;
import com.thaihoc.hotelbooking.entity.Review;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<String> createReview(@RequestBody ReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        reviewService.createReview(user, request);
        return ApiResponse.<String>builder().result("success").build();
    }

    // ✅ Lấy review theo branchId
    @GetMapping("/branch/{branchId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByBranch(@PathVariable String branchId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByBranch(branchId);
        return ApiResponse.<List<ReviewResponse>>builder().result(reviews).build();
    }

    // ✅ Lấy review theo roomTypeId (nếu không truyền thì lấy tất cả)
    @GetMapping("/roomtype")
    public ApiResponse<List<ReviewResponse>> getReviewsByRoomType(@RequestParam(required = false) Long roomTypeId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByRoomType(roomTypeId);
        return ApiResponse.<List<ReviewResponse>>builder().result(reviews).build();
    }

    @GetMapping("/branch/{branchId}/paged")
    public ApiResponse<Page<ReviewResponse>> getReviewsByBranchPaged(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> reviews = reviewService.getReviewsByBranchPaged(branchId, pageable);
        return ApiResponse.<Page<ReviewResponse>>builder().result(reviews).build();
    }


    @GetMapping("/admin")
    public ApiResponse<List<ReviewResponse>> getReviewsForAdmin(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Long roomTypeId
    ) {
        List<ReviewResponse> reviews = reviewService.getReviewsForAdmin(branchId, roomTypeId);
        return ApiResponse.<List<ReviewResponse>>builder().result(reviews).build();
    }

}

