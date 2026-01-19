package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.util.VnPayUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

@Log4j2
@Service
@Getter
public class VnPayService {

    @Value("${vnpay.tmnCode}") private String tmnCode;
    @Value("${vnpay.hashSecret}") private String hashSecret;
    @Value("${vnpay.payUrl}") private String payUrl;
    @Value("${vnpay.returnUrl}") private String returnUrl;
    @Value("${vnpay.ipnUrl}") private String ipnUrl;

    public String createPaymentUrl(long amountVnd, String orderInfo, String ipAddr, String txnRef) {
        // VNPay yêu cầu amount * 100
        String vnpAmount = String.valueOf(amountVnd * 100);

        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(tz);

        String vnpCreateDate = fmt.format(cal.getTime());
        cal.add(Calendar.MINUTE, 15);
        String vnpExpireDate = fmt.format(cal.getTime());

        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", vnpAmount);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef); // unique mỗi giao dịch
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", vnpCreateDate);
        params.put("vnp_ExpireDate", vnpExpireDate);

//        // Nếu bạn có public IPN URL để VNPay call:
//        params.put("vnp_IpnUrl", ipnUrl);

        String query = VnPayUtil.buildQueryString(params);
        String secureHash = VnPayUtil.hmacSHA512(hashSecret, query);

        String paymentUrl = payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

        // ✅ LOG DEBUG VNPay
        log.info("[VNPAY] tmnCode={}, txnRef={}, amountVnd={}, vnpAmount={}", tmnCode, txnRef, amountVnd, vnpAmount);
        log.info("[VNPAY] returnUrl={}", returnUrl);
        log.info("[VNPAY] ipnUrl={}", ipnUrl);
        log.info("[VNPAY] queryToSign={}", query);
        log.info("[VNPAY] secureHash={}", secureHash);
        log.info("[VNPAY] paymentUrl={}", paymentUrl);

        return paymentUrl;
    }
}

