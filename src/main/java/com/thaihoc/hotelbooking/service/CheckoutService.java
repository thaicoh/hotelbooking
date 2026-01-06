package com.thaihoc.hotelbooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.thaihoc.hotelbooking.dto.response.RoleResponse;
import com.thaihoc.hotelbooking.dto.response.RoomCheckoutResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeResponse;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.mapper.RoomTypeMapper;
import com.thaihoc.hotelbooking.mapper.UserMapper;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.util.BookingTimeUtil;
import com.thaihoc.hotelbooking.util.PriceCalculatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper  userMapper;

    @Autowired
    RoomTypeMapper roomTypeMapper;

    @Autowired
    BranchMapper branchMapper;

    public CheckoutService(RoomTypeRepository roomTypeRepository,
                           RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository,
                           RoomAvailabilityService roomAvailabilityService) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomTypeBookingTypePriceRepository = roomTypeBookingTypePriceRepository;
        this.roomAvailabilityService = roomAvailabilityService;
    }

    public RoomCheckoutResponse checkRoomAvailability(
            Long roomTypeId,
            String bookingTypeCode,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hours
    ) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND, "Room type not found"));

        LocalDateTime normalizedCheckIn = checkIn;
        LocalDateTime normalizedCheckOut = checkOut;
        if (normalizedCheckIn != null && normalizedCheckOut == null && hours != null && hours > 0) {
            normalizedCheckOut = normalizedCheckIn.plusHours(hours);
        }

        RoomTypeBookingTypePrice priceCfg = roomTypeBookingTypePriceRepository
                .findByRoomTypeIdAndBookingTypeCode(roomTypeId, bookingTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND,
                        "Không tìm thấy cấu hình giá cho roomTypeId=" + roomTypeId));

        if (normalizedCheckIn != null && normalizedCheckOut != null) {
            BookingTimeUtil.validateBookingTime(normalizedCheckIn, normalizedCheckOut, priceCfg.getBookingType());
        }

        boolean available = roomAvailabilityService.isRoomTypeAvailable(
                roomTypeId, normalizedCheckIn, normalizedCheckOut);

        BigDecimal computedPrice = PriceCalculatorUtil.computeSearchPrice(priceCfg,
                priceCfg.getBookingType(),
                normalizedCheckIn,
                normalizedCheckOut,
                hours);

        int availableRooms = roomAvailabilityService.countAvailableRooms(
                roomTypeId, normalizedCheckIn, normalizedCheckOut);

        // 👉 Lấy email từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        UserResponse userRes = userMapper.toUserResponse(user);

        return RoomCheckoutResponse.builder()
                .roomType(roomTypeMapper.toRoomTypeResponse(roomType))
                .branch(branchMapper.toBranchResponse(roomType.getBranch()))
                .bookingTypeCode(bookingTypeCode)
                .checkIn(normalizedCheckIn)
                .checkOut(normalizedCheckOut)
                .hours(hours)
                .price(computedPrice)
                .currency(priceCfg.getCurrency() != null ? priceCfg.getCurrency() : "VND")
                .availableRooms(availableRooms)
                .user(userRes) // thêm user vào response
                .build();
    }
}