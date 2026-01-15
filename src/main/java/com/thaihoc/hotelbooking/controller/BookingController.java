package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.AssignRoomRequest;
import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.request.UpdateBookingStatusRequest;
import com.thaihoc.hotelbooking.dto.response.*;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.service.BookingService;
import com.thaihoc.hotelbooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository  userRepository;

    // API tạo booking
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingCreationRequest req, HttpServletRequest http) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(req, http))
                .build();
    }



    @GetMapping("/admin/bookings")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF')")
    public ApiResponse<PageResponse<BookingListItemResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String bookingTypeCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPaid,
            @RequestParam(required = false) LocalDate checkInDate
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

        return ApiResponse.<PageResponse<BookingListItemResponse>>builder()
                .result(bookingService.getAllBookings(page, size, search, branchId, roomTypeId, bookingTypeCode, status, isPaid, checkInDate))
                .build();
    }


    @GetMapping("/my")
    public ApiResponse<List<BookingListItemResponse>> getMyBookings() {
        List<BookingListItemResponse> bookings = bookingService.getMyBookings();
        return ApiResponse.<List<BookingListItemResponse>>builder()
                .code(1000)
                .message("Success")
                .result(bookings)
                .build();
    }

    @GetMapping("/{bookingId}/available-rooms")
    public ApiResponse<RoomAvailabilityResponse> getAvailableRooms(@PathVariable Long bookingId) {
        return ApiResponse.<RoomAvailabilityResponse>builder()
                .result(bookingService.checkAvailableRooms(bookingId))
                .build();
    }

    @PostMapping("/assign-room")
    public ApiResponse<String> assignRoom(@RequestBody AssignRoomRequest request) {
        bookingService.assignRoomToBooking(request.getBookingId(), request.getRoomId());
        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }

    @DeleteMapping("/{bookingId}/room")
    public ApiResponse<String> removeRoom(@PathVariable Long bookingId) {
        bookingService.removeRoomFromBooking(bookingId);
        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }


    @PutMapping("/status")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF')")
    public ApiResponse<String> updateBookingStatus(@RequestBody UpdateBookingStatusRequest request) {
        bookingService.updateBookingStatus(request.getBookingId(), request.getStatus());
        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }


    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF','SCOPE_ROLE_CUSTOMER')")
    public ApiResponse<BookingDetailResponse> getBookingDetail(@PathVariable Long bookingId) {
        return ApiResponse.<BookingDetailResponse>builder()
                .result(bookingService.getBookingDetail(bookingId))
                .build();
    }

    @GetMapping("/branch/{branchId}/bookings")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF')")
    public ApiResponse<List<BookingListItemResponse>> getBookingsByBranchAndDate(
            @PathVariable String branchId,
            @RequestParam LocalDate date
    ) {
        return ApiResponse.<List<BookingListItemResponse>>builder()
                .result(bookingService.getBookingsByBranchAndDate(branchId, date))
                .build();
    }


    @PutMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CUSTOMER')")
    public ApiResponse<String> cancelBooking(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        bookingService.cancelBooking(bookingId, email);
        return ApiResponse.<String>builder()
                .result("Booking cancelled successfully")
                .build();
    }


}
