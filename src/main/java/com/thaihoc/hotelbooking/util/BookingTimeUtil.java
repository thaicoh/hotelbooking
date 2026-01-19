package com.thaihoc.hotelbooking.util;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

@Log4j2
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
        log.info("[VALIDATE] Bắt đầu kiểm tra giờ booking...");
        log.info("[VALIDATE] checkIn={}, checkOut={}, bookingType={}", checkIn, checkOut, bookingType.getCode());

        // 1. Kiểm tra ngày phải cùng một ngày
        if (!checkIn.toLocalDate().equals(checkOut.toLocalDate())) {
            log.error("[VALIDATE] Ngày checkIn={} và checkOut={} không cùng một ngày", checkIn.toLocalDate(), checkOut.toLocalDate());
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Ngày nhận phòng và trả phòng phải cùng một ngày.");
        }

        // 2. Kiểm tra giờ checkIn phải trước giờ checkOut
        if (!checkIn.isBefore(checkOut)) {
            log.error("[VALIDATE] Giờ checkIn={} không trước giờ checkOut={}", checkIn, checkOut);
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Giờ nhận phòng phải trước giờ trả phòng.");
        }

        // 3. Kiểm tra khung giờ cho phép theo BookingType
        if (bookingType.getDefaultCheckInTime() != null && bookingType.getDefaultCheckOutTime() != null) {
            int checkInHour = checkIn.getHour();
            int checkOutHour = checkOut.getHour();
            int defaultInHour = bookingType.getDefaultCheckInTime().getHour();
            int defaultOutHour = bookingType.getDefaultCheckOutTime().getHour();

            log.info("[VALIDATE] Giờ checkIn={}, checkOut={}, khung giờ cho phép từ {} đến {}",
                    checkInHour, checkOutHour, defaultInHour, defaultOutHour);

            // Kiểm tra giờ check-in
            if (checkInHour < defaultInHour || checkInHour > defaultOutHour) {
                log.error("[VALIDATE] Giờ nhận phòng {} không hợp lệ, ngoài khung {}-{}", checkInHour, defaultInHour, defaultOutHour);
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                        "Giờ nhận phòng không nằm trong khung giờ cho phép.");
            }

            // Kiểm tra giờ check-out
            if (checkOutHour < defaultInHour || checkOutHour > defaultOutHour) {
                log.error("[VALIDATE] Giờ trả phòng {} không hợp lệ, ngoài khung {}-{}", checkOutHour, defaultInHour, defaultOutHour);
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                        "Giờ trả phòng không nằm trong khung giờ cho phép.");
            }
        }

        // 4. Kiểm tra số giờ đặt không vượt quá 5
        long hours = ChronoUnit.HOURS.between(checkIn, checkOut);
        log.info("[VALIDATE] Khoảng thời gian đặt phòng = {} giờ", hours);

        if (hours > 5) {
            log.error("[VALIDATE] Số giờ đặt vượt quá giới hạn tối đa (5 giờ). Thực tế = {}", hours);
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                    "Số giờ đặt vượt quá giới hạn tối đa (5 giờ).");
        }

        log.info("[VALIDATE] Kiểm tra giờ booking thành công, hợp lệ.");
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

