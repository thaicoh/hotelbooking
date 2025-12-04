package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BookingMapper;
import com.thaihoc.hotelbooking.repository.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Log4j2
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private  RoomAvailabilityService roomAvailabilityService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public BookingResponse createBooking(BookingCreationRequest request) {
        log.info("Create booking request: roomTypeId={}, bookingTypeId={}, checkIn={}, checkOut={}, paymentMethod={}",
                request.getRoomTypeId(), request.getBookingTypeId(),
                request.getCheckInDate(), request.getCheckOutDate(),
                request.getPaymentMethod());



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        // ✅ Kiểm tra phòng trống trước khi tạo booking
        boolean available = roomAvailabilityService.isRoomTypeAvailable(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );



        if (!available) {
            throw new AppException(ErrorCode.BOOKING_ROOM_NOT_AVAILABLE);
        }

        RoomTypeBookingTypePrice priceConfig = roomTypeBookingTypePriceRepository
                .findByRoomType_IdAndBookingType_IdAndIsActive(
                        request.getRoomTypeId(),
                        request.getBookingTypeId(),
                        true
                )
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND));

        BigDecimal totalPrice = calculateTotalPrice(priceConfig, request.getCheckInDate(), request.getCheckOutDate());

        // Dùng mapper để convert request -> entity
        Booking booking = bookingMapper.toEntity(request);

        booking.setUser(user);
        booking.setRoomType(roomType);
        booking.setBookingType(priceConfig.getBookingType());
        booking.setTotalPrice(totalPrice);
        booking.setIsPaid(false);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setCreatedBy(user.getFullName());
        booking.setBookingReference(UUID.randomUUID().toString());

        // ✅ Phân nhánh theo phương thức thanh toán
        if ("PAY_AT_HOTEL".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("RESERVED");
        } else if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("PENDING");
        } else {
            throw new AppException(ErrorCode.BOOKING_PAYMENT_METHOD_INVALID);
        }
        log.info("Booking created: bookingReference={}, status={}, totalPrice={}, user={}",
                booking.getBookingReference(), booking.getStatus(),
                booking.getTotalPrice(), booking.getUser().getEmail());



        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }


    private BigDecimal calculateTotalPrice(RoomTypeBookingTypePrice priceConfig,
                                           LocalDateTime checkIn, LocalDateTime checkOut) {
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) {
            throw new RuntimeException("Invalid booking dates");
        }
        return priceConfig.getPrice().multiply(BigDecimal.valueOf(days));
    }


}
