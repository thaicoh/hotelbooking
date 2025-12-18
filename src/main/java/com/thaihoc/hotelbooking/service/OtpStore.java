package com.thaihoc.hotelbooking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OtpStore {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_KEY_PREFIX = "OTP:";
    private static final String VERIFIED_KEY_PREFIX = "VERIFIED:";

    // ✅ Lưu OTP vào Redis (TTL 5 phút)
    public void saveOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(5));
    }

    // ✅ Kiểm tra OTP
    public boolean verifyOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        Object storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp != null && storedOtp.equals(otp)) {

            // Đánh dấu email đã verify
            redisTemplate.opsForValue().set(
                    VERIFIED_KEY_PREFIX + email,
                    true,
                    Duration.ofMinutes(10) // verified có hiệu lực 10 phút
            );

            // Xóa OTP sau khi dùng
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    // ✅ Kiểm tra email đã verify chưa
    public boolean isVerified(String email) {
        Object verified = redisTemplate.opsForValue().get(VERIFIED_KEY_PREFIX + email);
        return verified != null && (boolean) verified;
    }

    // ✅ Xóa trạng thái verified sau khi đăng ký
    public void clearVerified(String email) {
        redisTemplate.delete(VERIFIED_KEY_PREFIX + email);
    }
}
