package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.entity.Booking;
import com.thaihoc.hotelbooking.entity.Payment;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.repository.BookingRepository;
import com.thaihoc.hotelbooking.repository.PaymentRepository;
import com.thaihoc.hotelbooking.service.EmailService;
import com.thaihoc.hotelbooking.service.VnPayService;
import com.thaihoc.hotelbooking.util.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/vnpay")
public class VnPayController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Autowired
    private  BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;


    private final VnPayService vnPayService;

    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    // 1) FE gọi endpoint này để lấy URL redirect sang VNPay
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody CreatePayReq req, HttpServletRequest http) {
        String ip = getClientIp(http);
        String txnRef = req.txnRef(); // tự generate UUID/timestamp từ FE/BE đều được
        String url = vnPayService.createPaymentUrl(req.amountVnd(), req.orderInfo(), ip, txnRef);
        return ResponseEntity.ok(Map.of("paymentUrl", url));
    }

    // 2) Return URL (VNPay redirect user về). Endpoint này optional nếu bạn returnUrl trỏ thẳng FE.
    // Nếu bạn muốn backend nhận rồi redirect FE kèm query:
    @GetMapping("/return")
    public void handleReturn(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        // Bạn có thể redirect về FE:
        String qs = request.getQueryString();
        response.sendRedirect("https://thaicoh.github.io/hotel-booking-frontend/checkout" + (qs != null ? ("?" + qs) : ""));
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {
        // Lấy tất cả params từ query string
        Map<String, String> rawParams = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> rawParams.put(k, v != null && v.length > 0 ? v[0] : null));

        // Lấy secureHash để kiểm tra
        String secureHash = rawParams.get("vnp_SecureHash");
        rawParams.remove("vnp_SecureHash");
        rawParams.remove("vnp_SecureHashType");

        // Tạo chữ ký lại để so sánh
        String query = VnPayUtil.buildQueryString(new TreeMap<>(rawParams));
        String expectedHash = VnPayUtil.hmacSHA512(vnPayService.getHashSecret(), query);

        if (!expectedHash.equalsIgnoreCase(secureHash)) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid signature"));
        }

        // Kiểm tra kết quả giao dịch
        if ("00".equals(rawParams.get("vnp_ResponseCode")) &&
                "00".equals(rawParams.get("vnp_TransactionStatus"))) {

            String txnRef = rawParams.get("vnp_TxnRef");
            Booking booking = bookingRepository.findByBookingReference(txnRef)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!Boolean.TRUE.equals(booking.getIsPaid())) {
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

                // ✅ Gửi email xác nhận đặt phòng thành công
                emailService.sendBookingConfirmation(
                        booking.getUser().getEmail(),
                        booking.getBookingReference(),
                        booking.getRoomType().getTypeName(),
                        booking.getCheckInDate().toLocalDate(),
                        booking.getCheckOutDate().toLocalDate(),
                        booking.getTotalPrice()
                );

            }

            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        }

        // Nếu thất bại
            return ResponseEntity.ok(Map.of("RspCode", "01", "Message", "Payment failed"));
    }



    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }


    public record CreatePayReq(long amountVnd, String orderInfo, String txnRef) {}
}

