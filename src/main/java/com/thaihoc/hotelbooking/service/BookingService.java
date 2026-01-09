package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.BookingListItemResponse;
import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.entity.*;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BookingMapper;
import com.thaihoc.hotelbooking.repository.*;
import com.thaihoc.hotelbooking.util.BookingTimeUtil;
import com.thaihoc.hotelbooking.util.PriceCalculatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingTypeRepository bookingTypeRepository;

    @Autowired
    private  RoomAvailabilityService roomAvailabilityService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VnPayService vnPayService;



    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public BookingResponse createBooking(BookingCreationRequest request, HttpServletRequest http) {
        log.info("Create booking request: roomTypeId={}, bookingTypeCode={}, checkIn={}, checkOut={}, paymentMethod={}",
                request.getRoomTypeId(), request.getBookingTypeCode(),
                request.getCheckInDate(), request.getCheckOutDate(),
                    request.getPaymentMethod());

        // 👉 Lấy user từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 👉 Lấy roomType
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        // 👉 Lấy bookingType
        BookingType bookingType = bookingTypeRepository.findByCode(request.getBookingTypeCode())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));

        // 👉 Chuẩn hóa checkOut nếu chỉ có checkIn + hours
        LocalDateTime normalizedCheckIn = request.getCheckInDate();
        LocalDateTime normalizedCheckOut = request.getCheckOutDate();
        if (normalizedCheckIn != null && normalizedCheckOut == null && request.getHours() != null && request.getHours() > 0) {
            normalizedCheckOut = normalizedCheckIn.plusHours(request.getHours());
        }

        // 👉 Validate thời gian
        if (normalizedCheckIn != null && normalizedCheckOut != null) {
            BookingTimeUtil.validateBookingTime(normalizedCheckIn, normalizedCheckOut, bookingType);
        }

        // 👉 Kiểm tra phòng trống
        boolean available = roomAvailabilityService.isRoomTypeAvailable(
                request.getRoomTypeId(), normalizedCheckIn, normalizedCheckOut);
        if (!available) {
            throw new AppException(ErrorCode.BOOKING_ROOM_NOT_AVAILABLE);
        }

        // 👉 Lấy config giá
        RoomTypeBookingTypePrice priceCfg = roomTypeBookingTypePriceRepository
                .findByRoomType_IdAndBookingType_IdAndIsActive(
                        request.getRoomTypeId(),
                        bookingType.getId(),
                        true
                )
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND));

        // 👉 Tính giá
        BigDecimal totalPrice = PriceCalculatorUtil.computeSearchPrice(
                priceCfg,
                bookingType,
                normalizedCheckIn,
                normalizedCheckOut,
                request.getHours()
        );

        // 👉 Convert request -> entity
        Booking booking = bookingMapper.toEntity(request);
        booking.setUser(user);
        booking.setRoomType(roomType);
        booking.setBookingType(priceCfg.getBookingType());
        booking.setTotalPrice(totalPrice);
        booking.setIsPaid(false);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setCreatedBy(user.getFullName());
        booking.setBookingReference(UUID.randomUUID().toString());
        booking.setCheckInDate(normalizedCheckIn);
        booking.setCheckOutDate(normalizedCheckOut);

        // 👉 Phân nhánh theo paymentMethod
        if ("PAY_AT_HOTEL".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus(BookingStatus.RESERVED);
        } else if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus(BookingStatus.PENDING);
            // ⏰ Set thời gian hết hạn
            booking.setExpireAt(LocalDateTime.now().plusMinutes(5));

        } else {
            throw new AppException(ErrorCode.BOOKING_PAYMENT_METHOD_INVALID);
        }

        bookingRepository.save(booking);

        log.info("Booking created: bookingReference={}, status={}, totalPrice={}, user={}",
                booking.getBookingReference(), booking.getStatus(),
                booking.getTotalPrice(), booking.getUser().getEmail());

        BookingResponse res = bookingMapper.toResponse(booking);

        if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod())) {
            String ipAddr = getClientIp(http);

            // vnp_TxnRef: dùng bookingReference để map IPN/Return về đúng booking
            String paymentUrl = vnPayService.createPaymentUrl(
                    booking.getTotalPrice().longValue(),                       // amount VND
                    "Thanh toan booking " + booking.getBookingReference(),     // orderInfo
                    ipAddr,
                    booking.getBookingReference()                              // txnRef
            );

            res.setPaymentUrl(paymentUrl);

            // (OPTIONAL) nếu bạn muốn lưu Payment record ngay lúc tạo link:
            // Payment p = new Payment();
            // p.setBooking(booking);
            // p.setPaymentMethod("VNPAY");
            // p.setPaymentStatus("PENDING");
            // p.setTxnRef(booking.getBookingReference());
            // p.setAmount(booking.getTotalPrice());
            // p.setPaymentDate(LocalDateTime.now());
            // paymentRepository.save(p);
        }

        return res;

    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }






    public PageResponse<BookingListItemResponse> getAllBookings(
            int page,
            int size,
            String search,
            String branchId,
            Long roomTypeId,
            String bookingTypeCode,
            String status,
            Boolean isPaid,
            LocalDate checkInDate
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // TODO: viết Specification hoặc query động để lọc theo các tham số
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        List<BookingListItemResponse> items = bookingPage.getContent().stream().map(booking -> {
            Payment latestPayment = paymentRepository
                    .findTopByBookingOrderByPaymentDateDesc(booking)
                    .orElse(null);

            return BookingListItemResponse.builder()
                    .bookingId(booking.getBookingId())
                    .bookingReference(booking.getBookingReference())
                    .customerName(booking.getUser().getFullName())
                    .customerPhone(booking.getUser().getPhone())
                    .branchName(booking.getRoomType().getBranch().getBranchName())
                    .roomTypeName(booking.getRoomType().getTypeName())
                    .bookingTypeName(booking.getBookingType().getName())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .totalPrice(booking.getTotalPrice())
                    .currency("VND") // hoặc lấy từ price config
                    .status(booking.getStatus().toString())
                    .isPaid(booking.getIsPaid())
                    .paymentStatus(latestPayment != null ? latestPayment.getPaymentStatus() : null)
                    .createdAt(booking.getCreatedAt())
                    .build();
        }).toList();

        return PageResponse.<BookingListItemResponse>builder()
                .items(items)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .build();
    }


    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public List<BookingListItemResponse> getMyBookings() {
        // 👉 Lấy user từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 👉 Lấy tất cả booking của user này
        List<Booking> bookings = bookingRepository.findByUser(user);

        // 👉 Convert sang response
        return bookings.stream().map(booking -> {
            Payment latestPayment = paymentRepository
                    .findTopByBookingOrderByPaymentDateDesc(booking)
                    .orElse(null);

            return BookingListItemResponse.builder()
                    .bookingId(booking.getBookingId())
                    .bookingReference(booking.getBookingReference())
                    .customerName(booking.getUser().getFullName())
                    .customerPhone(booking.getUser().getPhone())
                    .branchName(booking.getRoomType().getBranch().getBranchName())
                    .roomTypeName(booking.getRoomType().getTypeName())
                    .bookingTypeName(booking.getBookingType().getName())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .totalPrice(booking.getTotalPrice())
                    .currency("VND") // hoặc lấy từ price config
                    .status(booking.getStatus().toString())
                    .isPaid(booking.getIsPaid())
                    .paymentStatus(latestPayment != null ? latestPayment.getPaymentStatus() : null)
                    .createdAt(booking.getCreatedAt())
                    .build();
        }).toList();
    }

}
