package com.thaihoc.hotelbooking.util;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceCalculatorUtil {

    public static BigDecimal computeSearchPrice(
            RoomTypeBookingTypePrice priceCfg,
            BookingType bookingType,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hoursInput
    ) {
        BigDecimal basePrice = priceCfg.getPrice();
        if (basePrice == null) return null;

        // Có đủ thời gian => tính đúng
        if (checkIn != null && checkOut != null) {
            return calculateTotalPrice(priceCfg, checkIn, checkOut, bookingType);
        }

        // Không có thời gian => trả về starting price
        if ("HOUR".equals(bookingType.getCode())) {
            int h = (hoursInput != null && hoursInput > 0) ? hoursInput : 1;

            if (priceCfg.getMaxHours() != null && h > priceCfg.getMaxHours()) return null;
            if (h > 5) return null; // rule hiện tại

            BigDecimal total = basePrice;
            if (h > 1 && priceCfg.getAdditionalHourPrice() != null) {
                total = total.add(priceCfg.getAdditionalHourPrice().multiply(BigDecimal.valueOf(h - 1)));
            }
            return total;
        }

        // DAY / NIGHT
        return basePrice;
    }

    private static BigDecimal calculateTotalPrice(RoomTypeBookingTypePrice priceCfg,
                                                  LocalDateTime checkIn,
                                                  LocalDateTime checkOut,
                                                  BookingType bookingType) {
        // Logic tính giá theo khoảng thời gian
        long hours = java.time.Duration.between(checkIn, checkOut).toHours();
        if ("HOUR".equals(bookingType.getCode())) {
            BigDecimal total = priceCfg.getPrice();
            if (hours > 1 && priceCfg.getAdditionalHourPrice() != null) {
                total = total.add(priceCfg.getAdditionalHourPrice().multiply(BigDecimal.valueOf(hours - 1)));
            }
            return total;
        }
        return priceCfg.getPrice();
    }
}
