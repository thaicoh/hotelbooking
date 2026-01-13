package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.response.RevenueStatisticResponse;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final BookingRepository bookingRepository;

    /**
     * @param type: "YEARLY", "MONTHLY", "DAILY"
     * @param year: Bắt buộc nếu type là MONTHLY hoặc DAILY
     * @param month: Bắt buộc nếu type là DAILY
     * @param branchId: Tùy chọn (null = tất cả chi nhánh)
     */
    public List<RevenueStatisticResponse> getRevenueStatistics(String type, Integer year, Integer month, String branchId) {
        if (branchId != null && branchId.trim().isEmpty()) {
            branchId = null; // Chuẩn hóa chuỗi rỗng thành null
        }

        switch (type.toUpperCase()) {
            case "YEARLY":
                // Trả về doanh thu của các năm
                return bookingRepository.getRevenueByYear(branchId);

            case "MONTHLY":
                if (year == null) {
                    throw new IllegalArgumentException("Year is required for monthly statistics");
                }
                // Trả về doanh thu 12 tháng của năm `year`
                return bookingRepository.getRevenueByMonth(year, branchId);

            case "DAILY":
                if (year == null || month == null) {
                    throw new IllegalArgumentException("Year and Month are required for daily statistics");
                }
                // Trả về doanh thu các ngày trong `month`/`year`
                return bookingRepository.getRevenueByDay(year, month, branchId);

            default:
                return Collections.emptyList();
        }
    }
}