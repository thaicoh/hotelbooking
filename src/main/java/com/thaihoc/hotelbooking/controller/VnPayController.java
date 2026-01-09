package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.service.VnPayService;
import com.thaihoc.hotelbooking.util.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/vnpay")
public class VnPayController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

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

    // 3) IPN: VNPay gọi server-to-server
    @GetMapping("/ipn")
    public ResponseEntity<String> ipn(HttpServletRequest request) {
        // Lấy tất cả params
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v != null && v.length > 0 ? v[0] : null));

        // Validate chữ ký
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        SortedMap<String, String> sorted = new TreeMap<>(params);
        String query = VnPayUtil.buildQueryString(sorted);
        String signed = VnPayUtil.hmacSHA512(hashSecret, query);

        if (!signed.equalsIgnoreCase(secureHash)) {
            return ResponseEntity.ok("{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}");
        }

        // TODO: update DB theo vnp_TxnRef / vnp_ResponseCode / vnp_TransactionStatus
        // - vnp_ResponseCode = "00" thường là OK
        // - vnp_TransactionStatus = "00" thường là success

        return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }


    public record CreatePayReq(long amountVnd, String orderInfo, String txnRef) {}
}

