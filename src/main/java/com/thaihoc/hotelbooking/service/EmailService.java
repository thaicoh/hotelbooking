package com.thaihoc.hotelbooking.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP xác thực");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã này hết hạn sau 5 phút.");

        mailSender.send(message);
    }


    @Async
    public void sendBookingConfirmation(String toEmail, String bookingCode, String hotelName,
                                        LocalDate checkInDate, LocalDate checkOutDate, BigDecimal totalPrice) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt phòng thành công");

            String htmlContent = """
            <html>
              <body style="font-family: Arial, sans-serif; line-height:1.6;">
                <h2 style="color:#2E86C1;">Xác nhận đặt phòng thành công</h2>
                <p>Xin chào,</p>
                <p>Bạn đã đặt phòng thành công tại khách sạn <strong>%s</strong>.</p>
                <table style="border-collapse: collapse; width: 100%%; margin-top:10px;">
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Mã đặt phòng</td>
                    <td style="border:1px solid #ddd; padding:8px;"><strong>%s</strong></td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Ngày nhận phòng</td>
                    <td style="border:1px solid #ddd; padding:8px;">%s</td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Ngày trả phòng</td>
                    <td style="border:1px solid #ddd; padding:8px;">%s</td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Tổng số tiền</td>
                    <td style="border:1px solid #ddd; padding:8px; color:#E74C3C;"><strong>%s VND</strong></td>
                  </tr>
                </table>
                <p style="margin-top:15px;">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.<br/>
                Chúc bạn có trải nghiệm thoải mái!</p>
              </body>
            </html>
            """.formatted(hotelName, bookingCode, checkInDate, checkOutDate, totalPrice);

            helper.setText(htmlContent, true); // true = gửi HTML

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // log lỗi nhưng không chặn luồng chính
            log.error("Gửi email xác nhận thất bại", e);
        }
    }



    @Async
    public void sendBookingCancellation(String toEmail, String bookingCode, String hotelName,
                                        LocalDate checkInDate, LocalDate checkOutDate, BigDecimal totalPrice) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Thông báo hủy đặt phòng");

            String htmlContent = """
            <html>
              <body style="font-family: Arial, sans-serif; line-height:1.6;">
                <h2 style="color:#E74C3C;">Đặt phòng đã bị hủy</h2>
                <p>Xin chào,</p>
                <p>Đặt phòng của bạn tại khách sạn <strong>%s</strong> đã được hủy.</p>
                <table style="border-collapse: collapse; width: 100%%; margin-top:10px;">
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Mã đặt phòng</td>
                    <td style="border:1px solid #ddd; padding:8px;"><strong>%s</strong></td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Ngày nhận phòng dự kiến</td>
                    <td style="border:1px solid #ddd; padding:8px;">%s</td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Ngày trả phòng dự kiến</td>
                    <td style="border:1px solid #ddd; padding:8px;">%s</td>
                  </tr>
                  <tr>
                    <td style="border:1px solid #ddd; padding:8px;">Tổng số tiền dự kiến</td>
                    <td style="border:1px solid #ddd; padding:8px; color:#2E86C1;"><strong>%s VND</strong></td>
                  </tr>
                </table>
                <p style="margin-top:15px;">Nếu bạn có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ bộ phận chăm sóc khách hàng của chúng tôi.</p>
                <p>Rất mong được phục vụ bạn trong những lần đặt phòng tiếp theo.</p>
              </body>
            </html>
            """.formatted(hotelName, bookingCode, checkInDate, checkOutDate, totalPrice);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Gửi email hủy đặt phòng thất bại", e);
        }
    }

}
