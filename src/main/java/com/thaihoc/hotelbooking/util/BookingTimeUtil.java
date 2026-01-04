package com.thaihoc.hotelbooking.util;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BookingTimeUtil {

    public static void validateBookingTime(LocalDateTime checkIn, LocalDateTime checkOut, BookingType bookingType) {
        LocalDateTime now = LocalDateTime.now();

        if (checkOut.isBefore(now)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Thời gian trả phòng phải sau thời điểm hiện tại. (now=" + now + ", checkOut=" + checkOut + ")");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Thời gian trả phòng phải sau thời gian nhận phòng.");
        }

        String code = bookingType.getCode();

        switch (code) {
            case "HOUR" -> validateHourBooking(checkIn, checkOut, bookingType);
            case "NIGHT" -> validateNightBooking(checkIn, checkOut);
            case "DAY" -> validateDayBooking(checkIn, checkOut);
            default -> throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Loại đặt phòng không hợp lệ: " + code);
        }
    }

    private static void validateHourBooking(LocalDateTime checkIn, LocalDateTime checkOut, BookingType bookingType) {
        if (bookingType.getDefaultCheckInTime() != null && bookingType.getDefaultCheckOutTime() != null) {
            int h = checkIn.getHour();
            if (h < bookingType.getDefaultCheckInTime().getHour()
                    || h > bookingType.getDefaultCheckOutTime().getHour()) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                        "Giờ nhận phòng không nằm trong khung giờ cho phép.");
            }
        }

        long hours = ChronoUnit.HOURS.between(checkIn, checkOut);
        if (hours > 5) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Số giờ đặt vượt quá giới hạn tối đa (5 giờ).");
        }
    }

    private static void validateNightBooking(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn.getHour() < 21) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Giờ nhận phòng cho loại NIGHT phải từ 21:00.");
        }
        if (checkOut.getHour() != 12) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Giờ trả phòng cho loại NIGHT phải là 12:00.");
        }
    }

    private static void validateDayBooking(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn.getHour() != 14) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Giờ nhận phòng cho loại DAY phải là 14:00.");
        }
        if (checkOut.getHour() != 12) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Giờ trả phòng cho loại DAY phải là 12:00.");
        }
        if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Ngày trả phòng phải sau ngày nhận phòng cho loại DAY.");
        }
    }
}

