package com.thaihoc.hotelbooking.util;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PriceCalculatorUtil {

    public static BigDecimal computeSearchPrice(
            RoomTypeBookingTypePrice priceCfg,
            BookingType bookingType,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hoursInput
    ) {
        BigDecimal basePrice = priceCfg.getPrice();
        if (basePrice == null) {
            System.out.println("[PRICE] Base price is null => return null");
            return null;
        }

        String code = bookingType != null ? bookingType.getCode() : null;
        System.out.println("[PRICE] bookingType=" + code + ", checkIn=" + checkIn + ", checkOut=" + checkOut + ", hoursInput=" + hoursInput);

        // Có đủ thời gian => tính đúng (chỉ áp dụng DAY/NIGHT; HOUR sẽ xử lý theo hoursInput)
        if (checkIn != null && checkOut != null && !"HOUR".equals(code)) {
            System.out.println("[PRICE] DAY/NIGHT with full time => delegate to calculateTotalPrice");
            return calculateTotalPrice(priceCfg, checkIn, checkOut, bookingType);
        }

        // HOUR: dùng checkIn + hoursInput
        if ("HOUR".equals(code)) {
            int hours = (hoursInput != null && hoursInput > 0) ? hoursInput : 0;
            if (hours == 0) {
                System.out.println("[PRICE][HOUR] Invalid hoursInput=" + hoursInput + " => return basePrice (starting price)");
                return basePrice; // hoặc return null nếu muốn bắt buộc hoursInput
            }

            if (priceCfg.getMaxHours() != null && hours > priceCfg.getMaxHours()) {
                System.out.println("[PRICE][HOUR] Exceed maxHours=" + priceCfg.getMaxHours() + " with hours=" + hours + " => return null");
                return null;
            }
            if (hours > 5) {
                System.out.println("[PRICE][HOUR] Exceed rule limit (5) with hours=" + hours + " => return null");
                return null;
            }

            BigDecimal total = basePrice;
            System.out.println("[PRICE][HOUR] First hour = basePrice=" + basePrice);

            if (hours > 1) {
                if (priceCfg.getAdditionalHourPrice() == null) {
                    System.out.println("[PRICE][HOUR] additionalHourPrice is null, cannot add for extra hours, total=" + total);
                } else {
                    BigDecimal extra = priceCfg.getAdditionalHourPrice().multiply(BigDecimal.valueOf(hours - 1));
                    total = total.add(extra);
                    System.out.println("[PRICE][HOUR] Extra hours=" + (hours - 1) + ", additionalHourPrice=" + priceCfg.getAdditionalHourPrice() + ", extraSum=" + extra + ", total=" + total);
                }
            }

            if (checkIn == null) {
                System.out.println("[PRICE][HOUR] checkIn is null => skip weekend surcharge");
            } else {
                DayOfWeek dow = checkIn.toLocalDate().getDayOfWeek();
                if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) && priceCfg.getWeekendSurcharge() != null) {
                    total = total.add(priceCfg.getWeekendSurcharge());
                    System.out.println("[PRICE][HOUR] Weekend (" + dow + ") surcharge=" + priceCfg.getWeekendSurcharge() + ", total=" + total);
                } else {
                    System.out.println("[PRICE][HOUR] No weekend surcharge (dow=" + dow + ", weekendSurcharge=" + priceCfg.getWeekendSurcharge() + ")");
                }
            }

            System.out.println("[PRICE][HOUR] Final total=" + total);
            return total;
        }

        // DAY / NIGHT không có thời gian thì trả về basePrice
        System.out.println("[PRICE] Non-HOUR without full time => return basePrice=" + basePrice);
        return basePrice;
    }

    private static BigDecimal calculateTotalPrice(RoomTypeBookingTypePrice priceCfg,
                                                  LocalDateTime checkIn,
                                                  LocalDateTime checkOut,
                                                  BookingType bookingType) {
        if ("DAY".equals(bookingType.getCode())) {
            if (checkIn.getHour() != 14 || checkOut.getHour() != 12) {
                return null; // không hợp lệ
            }

            long days = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
            if (days < 1) return null;

            BigDecimal total = BigDecimal.ZERO;
            LocalDate current = checkIn.toLocalDate();

            for (int i = 0; i < days; i++) {
                BigDecimal dayPrice = priceCfg.getPrice();
                DayOfWeek dow = current.getDayOfWeek();
                if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
                        && priceCfg.getWeekendSurcharge() != null) {
                    dayPrice = dayPrice.add(priceCfg.getWeekendSurcharge());
                }
                total = total.add(dayPrice);
                current = current.plusDays(1);
            }

            return total;
        }

        if ("NIGHT".equals(bookingType.getCode())) {
            // Checkin phải 21h, checkout 12h hôm sau
            if (checkIn.getHour() != 21 || checkOut.getHour() != 12) {
                return null; // không hợp lệ
            }

            BigDecimal total = priceCfg.getPrice();
            DayOfWeek dow = checkIn.toLocalDate().getDayOfWeek();
            if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
                    && priceCfg.getWeekendSurcharge() != null) {
                total = total.add(priceCfg.getWeekendSurcharge());
            }
            return total;
        }

        return priceCfg.getPrice();
    }
}