package com.thaihoc.hotelbooking.enums;

public enum BookingStatus {
    PENDING,        // Đặt online, chờ thanh toán, sẽ chuyển sang CANCELLED nếu sau 5p chưa thanh toán
    RESERVED,       // Giữ phòng, thanh toán tại khách sạn
    PAID,           // Thanh toán thành công, cả online lẫn tại khách sạn
    CANCELLED,      // Đơn bị hủy (khách hủy hoặc hết hạn)
    CHECKED_IN,     // Khách đã nhận phòng, mặc định là thanh toán thành công
    CHECKED_OUT     // Khách đã trả phòng, mặc định là thanh toán thành công
}

