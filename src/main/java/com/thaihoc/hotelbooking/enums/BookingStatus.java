package com.thaihoc.hotelbooking.enums;

public enum BookingStatus {
    PENDING,        // Đặt online, chờ thanh toán
    RESERVED,       // Giữ phòng, thanh toán tại khách sạn
    CONFIRMED,      // Đã xác nhận (có thể đã thanh toán hoặc được duyệt)
    PAID,           // Thanh toán thành công
    CANCELLED,      // Đơn bị hủy (khách hủy hoặc hết hạn)
    FAILED_PAYMENT, // Thanh toán thất bại
    CHECKED_IN,     // Khách đã nhận phòng
    CHECKED_OUT,     // Khách đã trả phòng
    COMPLETED // Booking đã hoàn thành (checkout xong, hết hạn sử dụng phòng)
}

