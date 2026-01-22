package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.ReviewRequest;
import com.thaihoc.hotelbooking.dto.response.ReviewResponse;
import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Review;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.ReviewMapper;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import com.thaihoc.hotelbooking.repository.ReviewRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    private final UserRepository  userRepository;

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CUSTOMER')")
    public void createReview(User user, ReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZE, "Bạn không thể đánh giá booking này.");
        }

        // Kiểm tra trạng thái booking
        if (booking.getStatus() != BookingStatus.CHECKED_OUT || !booking.getIsPaid()) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Chỉ được đánh giá booking đã check-out và đã thanh toán.");
        }

        // Kiểm tra đã đánh giá chưa
        if (reviewRepository.existsByBooking(booking)) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Booking đã được đánh giá.");
        }

        Review review = Review.builder()
                .user(user)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    public List<ReviewResponse> getReviewsByBranch(String branchId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return reviewMapper.toReviewResponseList(reviewRepository.findAllByBranchId(branchId));
    }

    public List<ReviewResponse> getReviewsByRoomType(Long roomTypeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy danh sách quyền của user
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("SCOPE_ROLE_ADMIN"));
        boolean isStaff = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("SCOPE_ROLE_STAFF"));

        if (roomTypeId == null) {
            if (isAdmin) {
                // ADMIN: lấy tất cả review không cần kiểm tra branch
                return reviewMapper.toReviewResponseList(reviewRepository.findAll());
            } else if (isStaff) {
                // STAFF: chỉ lấy review theo branch của staff
                if (user.getBranch() == null) {
                    throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Staff chưa được gán branch");
                }
                return reviewMapper.toReviewResponseList(
                        reviewRepository.findAllByBranchId(user.getBranch().getId())
                );
            } else {
                throw new AppException(ErrorCode.UNAUTHORIZE, "Bạn không có quyền xem review theo branch");
            }
        }

        // Nếu có roomTypeId thì lấy theo roomTypeId (cả ADMIN và STAFF đều được)
        return reviewMapper.toReviewResponseList(reviewRepository.findAllByRoomTypeId(roomTypeId));
    }

    public Page<ReviewResponse> getReviewsByBranchPaged(String branchId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAllByBranchId(branchId, pageable);
        List<ReviewResponse> responseList = reviewMapper.toReviewResponseList(reviewPage.getContent());
        return new PageImpl<>(responseList, pageable, reviewPage.getTotalElements());
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<ReviewResponse> getReviewsForAdmin(String branchId, Long roomTypeId) {
        // Nếu không truyền gì → lấy tất cả review
        if (branchId == null && roomTypeId == null) {
            return reviewMapper.toReviewResponseList(reviewRepository.findAll());
        }

        // Nếu chỉ truyền branchId → lấy tất cả review trong branch đó
        if (branchId != null && roomTypeId == null) {
            return reviewMapper.toReviewResponseList(reviewRepository.findAllByBranchId(branchId));
        }

        // Nếu chỉ truyền roomTypeId → lấy review theo roomType
        if (roomTypeId != null && branchId == null) {
            return reviewMapper.toReviewResponseList(reviewRepository.findAllByRoomTypeId(roomTypeId));
        }

        // Nếu truyền cả branchId và roomTypeId → lấy review trong roomType thuộc branch đó
        return reviewMapper.toReviewResponseList(
                reviewRepository.findAllByBranchIdAndRoomTypeId(branchId, roomTypeId)
        );
    }

}
