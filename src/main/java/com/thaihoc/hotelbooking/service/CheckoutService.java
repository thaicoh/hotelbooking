package com.thaihoc.hotelbooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.thaihoc.hotelbooking.dto.response.RoomCheckoutResponse;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import com.thaihoc.hotelbooking.util.BookingTimeUtil;
import com.thaihoc.hotelbooking.util.PriceCalculatorUtil;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository;
    private final RoomAvailabilityService roomAvailabilityService;

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

        // Chuẩn hoá checkOut nếu chỉ truyền checkIn + hours
        LocalDateTime normalizedCheckIn = checkIn;
        LocalDateTime normalizedCheckOut = checkOut;
        if (normalizedCheckIn != null && normalizedCheckOut == null && hours != null && hours > 0) {
            normalizedCheckOut = normalizedCheckIn.plusHours(hours);
        }

        // Lấy config giá theo bookingType
        RoomTypeBookingTypePrice priceCfg = roomTypeBookingTypePriceRepository
                .findByRoomTypeIdAndBookingTypeCode(roomTypeId, bookingTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND,
                        "Không tìm thấy cấu hình giá cho roomTypeId=" + roomTypeId));

        // Validate thời gian đặt phòng
        if (normalizedCheckIn != null && normalizedCheckOut != null) {
            BookingTimeUtil.validateBookingTime(normalizedCheckIn, normalizedCheckOut, priceCfg.getBookingType());
        }

        // Kiểm tra phòng trống
        boolean available = roomAvailabilityService.isRoomTypeAvailable(
                roomTypeId, normalizedCheckIn, normalizedCheckOut);

        if (!available) {
            return RoomCheckoutResponse.builder()
                    .roomTypeId(roomTypeId)
                    .roomTypeName(roomType.getTypeName())
                    .price(BigDecimal.ZERO)
                    .currency(priceCfg.getCurrency() != null ? priceCfg.getCurrency() : "VND")
                    .availableRooms(0)
                    .build();
        }

        // Tính giá
        BigDecimal computedPrice = PriceCalculatorUtil.computeSearchPrice(priceCfg,
                priceCfg.getBookingType(),
                normalizedCheckIn,
                normalizedCheckOut,
                hours);

        // Lấy số lượng phòng còn trống
        int availableRooms = roomAvailabilityService.countAvailableRooms(
                roomTypeId, normalizedCheckIn, normalizedCheckOut);

        return RoomCheckoutResponse.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName(roomType.getTypeName())
                .price(computedPrice)
                .currency(priceCfg.getCurrency() != null ? priceCfg.getCurrency() : "VND")
                .availableRooms(availableRooms)
                .build();
    }
}