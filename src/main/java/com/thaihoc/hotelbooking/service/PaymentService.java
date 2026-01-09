package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Payment;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import com.thaihoc.hotelbooking.repository.PaymentRepository;
import com.thaihoc.hotelbooking.util.VnPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VnPayService vnPayService;


    public boolean processVnPayCallback(Map<String, String> rawParams) {
        String secureHash = rawParams.get("vnp_SecureHash");
        rawParams.remove("vnp_SecureHash");
        rawParams.remove("vnp_SecureHashType");

        String query = VnPayUtil.buildQueryString(new TreeMap<>(rawParams));
        String expectedHash = VnPayUtil.hmacSHA512(vnPayService.getHashSecret(), query);

        if (!expectedHash.equalsIgnoreCase(secureHash)) {
            return false;
        }

        if ("00".equals(rawParams.get("vnp_ResponseCode")) &&
                "00".equals(rawParams.get("vnp_TransactionStatus"))) {

            String txnRef = rawParams.get("vnp_TxnRef");
            Booking booking = bookingRepository.findByBookingReference(txnRef)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Nếu booking đã thanh toán rồi thì không tạo thêm Payment
            if (Boolean.TRUE.equals(booking.getIsPaid())) {
                return true; // coi như thành công, không cần tạo lại
            }

            booking.setIsPaid(true);
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            LocalDateTime now = LocalDateTime.now();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(booking.getTotalPrice())
                    .currency("VND")
                    .paymentMethod("VNPAY")
                    .paymentStatus("SUCCESS")
                    .transactionPreference(rawParams.get("vnp_CardType"))
                    .paymentDate(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .notes("VNPay TransactionNo=" + rawParams.get("vnp_TransactionNo"))
                    .build();

            paymentRepository.save(payment);

            return true;
        }

        return false;
    }

}
