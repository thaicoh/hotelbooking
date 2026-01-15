package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.*;
import com.thaihoc.hotelbooking.entity.*;
import com.thaihoc.hotelbooking.enums.BookingStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BookingMapper;
import com.thaihoc.hotelbooking.mapper.RoomMapper;
import com.thaihoc.hotelbooking.repository.*;
import com.thaihoc.hotelbooking.util.BookingTimeUtil;
import com.thaihoc.hotelbooking.util.PriceCalculatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import java.util.Optional;
import java.util.Random;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    private String generateUniqueBookingReference() {
        String reference;
        do {
            // Sinh số ngẫu nhiên 10 chữ số
            reference = String.format("%010d", new Random().nextLong() % 1_000_000_0000L);
            if (reference.startsWith("-")) {
                reference = reference.substring(1); // loại bỏ dấu âm nếu có
            }
        } while (bookingRepository.findByBookingReference(reference).isPresent());
        return reference;
    }




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
        booking.setBookingReference(generateUniqueBookingReference());
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

        BookingResponse res = bookingMapper.toResponse(booking);

        // Gửi cho admin (toàn hệ thống)
        messagingTemplate.convertAndSend("/topic/bookings", res);

        // Gửi cho staff của chi nhánh tương ứng
        String branchId = booking.getRoomType().getBranch().getId();
        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/bookings", res);


        log.info("Booking created: bookingReference={}, status={}, totalPrice={}, user={}",
                booking.getBookingReference(), booking.getStatus(),
                booking.getTotalPrice(), booking.getUser().getEmail());

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

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN','SCOPE_ROLE_STAFF')")
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

        Page<Booking> bookingPage = bookingRepository.searchBookings(
                search,
                branchId,
                roomTypeId,
                bookingTypeCode,
                status != null ? BookingStatus.valueOf(status) : null,
                isPaid,
                checkInDate,
                pageable
        );

        List<BookingListItemResponse> items = bookingPage.getContent().stream().map(booking -> {
            Payment latestPayment = paymentRepository
                    .findTopByBookingOrderByPaymentDateDesc(booking)
                    .orElse(null);

            Room room = booking.getRoom();


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
                    .roomId(room != null ? room.getRoomId() : null)
                    .roomNumber(room != null ? room.getRoomNumber() : null)
                    .bookingTypeCode(booking.getBookingType().getCode())
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



        return bookings.stream().map(booking -> {
            Payment latestPayment = paymentRepository
                    .findTopByBookingOrderByPaymentDateDesc(booking)
                    .orElse(null);

            Optional<Review> optionalReview = reviewRepository.findByBooking_BookingId(booking.getBookingId());

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
                    // ✅ xử lý Optional an toàn
                    .reviewed(optionalReview.isPresent())
                    .rating(optionalReview.map(Review::getRating).orElse(null))
                    .paymentStatus(latestPayment != null ? latestPayment.getPaymentStatus() : null)
                    .createdAt(booking.getCreatedAt())
                    .build();
        }).toList();
    }

    public RoomAvailabilityResponse checkAvailableRooms(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Long roomTypeId = booking.getRoomType().getId();
        LocalDateTime checkIn = booking.getCheckInDate();
        LocalDateTime checkOut = booking.getCheckOutDate();

        // trạng thái được coi là đang chiếm phòng
        List<BookingStatus> activeStatuses = List.of(BookingStatus.CHECKED_IN, BookingStatus.RESERVED, BookingStatus.PAID);

        log.info("BookingId={}, roomTypeId={}, checkIn={}, checkOut={}",
                bookingId, roomTypeId, checkIn, checkOut);
        log.info("Active statuses={}", activeStatuses);

        List<Room> allRooms = roomRepository.findByRoomTypeIdOrderByRoomNumberDesc(roomTypeId);
        List<Room> availableRooms = roomRepository.findAvailableRoomsByRoomTypeAndDateRange(
                roomTypeId, activeStatuses, checkIn, checkOut
        );

        log.info("All rooms size={}, available rooms size={}",
                allRooms.size(), availableRooms.size());
        availableRooms.forEach(r -> log.info("Available room: id={}, number={}", r.getRoomId(), r.getRoomNumber()));

        return RoomAvailabilityResponse.builder()
                .allRooms(roomMapper.toRoomResponseList(allRooms))
                .availableRooms(roomMapper.toRoomResponseList(availableRooms))
                .build();
    }


    @Transactional
    public void assignRoomToBooking(Long bookingId, String roomId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        // Kiểm tra roomType
        if (!room.getRoomType().getId().equals(booking.getRoomType().getId())) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Room type does not match booking room type");
        }

        // Lấy danh sách phòng khả dụng trong khoảng thời gian của booking
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.CHECKED_IN,
                BookingStatus.RESERVED,
                BookingStatus.PAID
        );

        List<Room> availableRooms = roomRepository.findAvailableRoomsByRoomTypeAndDateRange(
                booking.getRoomType().getId(),
                activeStatuses,
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        // Kiểm tra room gửi lên có nằm trong danh sách availableRooms không
        boolean isAvailable = availableRooms.stream()
                .anyMatch(r -> r.getRoomId().equals(roomId));

        if (!isAvailable) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Room is not available in this time range");
        }

        // Gán phòng cho booking
        booking.setRoom(room);
        bookingRepository.save(booking);
    }

    @Transactional
    public void removeRoomFromBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getRoom() == null) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Booking has no room assigned");
        }

        booking.setRoom(null);
        bookingRepository.save(booking);

    }


    @Transactional
    public void updateBookingStatus(Long bookingId, String statusStr) {
        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Invalid booking status: " + statusStr);
        }

        // Không cho chỉnh thành PENDING
        if (newStatus == BookingStatus.PENDING) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Cannot change status to PENDING");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Lấy danh sách payment của booking
        List<Payment> payments = paymentRepository.findByBooking(booking);

        boolean hasVNPayPayment = payments.stream()
                .anyMatch(p -> "VNPAY".equalsIgnoreCase(p.getPaymentMethod()));

        // Nếu có VNPay payment thì không cho chỉnh thành RESERVED hoặc CANCELLED
        if (hasVNPayPayment && (newStatus == BookingStatus.RESERVED || newStatus == BookingStatus.CANCELLED)) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION,
                    "Cannot change status to " + newStatus + " when booking has VNPay payment");
        }

        // Nếu chuyển sang PAID / CHECKED_IN / CHECKED_OUT
        if (newStatus == BookingStatus.PAID
                || newStatus == BookingStatus.CHECKED_IN
                || newStatus == BookingStatus.CHECKED_OUT) {

            // Kiểm tra đã có payment và isPaid chưa
            boolean alreadyPaid = booking.getIsPaid() != null && booking.getIsPaid();
            boolean hasAnyPayment = !payments.isEmpty();

            if (!alreadyPaid || !hasAnyPayment) {
                // Tạo payment mới
                Payment payment = new Payment();
                payment.setBooking(booking);
                payment.setAmount(booking.getTotalPrice());
                payment.setCurrency("VND");
                payment.setPaymentMethod("CASH_AT_HOTEL"); // hoặc "CASH"
                payment.setPaymentStatus("SUCCESS");
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);

                // Đánh dấu booking đã thanh toán
                booking.setIsPaid(true);
            }
        }

        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional()
    public BookingDetailResponse getBookingDetail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Lấy danh sách payment của booking
        List<Payment> payments = paymentRepository.findByBooking(booking);
        Optional<Review> optionalReview = reviewRepository.findByBooking_BookingId(booking.getBookingId());


        return BookingDetailResponse.builder()
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
                .currency("VND")
                .status(booking.getStatus().toString())
                .isPaid(booking.getIsPaid())
                .createdAt(booking.getCreatedAt())
                .roomId(booking.getRoom() != null ? booking.getRoom().getRoomId() : null)
                .roomNumber(booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null)

                // ✅ xử lý Optional an toàn
                .reviewed(optionalReview.isPresent())
                .rating(optionalReview.map(Review::getRating).orElse(null))



                .payments(payments.stream().map(p -> PaymentResponse.builder()
                        .paymentId(p.getPaymentId())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .paymentMethod(p.getPaymentMethod())
                        .paymentStatus(p.getPaymentStatus())
                        .transactionPreference(p.getTransactionPreference())
                        .paymentDate(p.getPaymentDate())
                        .notes(p.getNotes())
                        .build()
                ).toList())
                .build();
    }


    public List<BookingListItemResponse> getBookingsByBranchAndDate(String branchId, LocalDate date) {
        List<BookingStatus> excludedStatuses = List.of(BookingStatus.PENDING, BookingStatus.CANCELLED);

        List<Booking> bookings = bookingRepository.findBookingsByBranchAndDate(branchId, date, excludedStatuses);

        return bookings.stream().map(booking -> {
            Room room = booking.getRoom();
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
                    .currency("VND")
                    .status(booking.getStatus().toString())
                    .isPaid(booking.getIsPaid())
                    .roomId(room != null ? room.getRoomId() : null)
                    .roomNumber(room != null ? room.getRoomNumber() : null)
                    .build();
        }).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CUSTOMER')")
    public void cancelBooking(Long bookingId, String userEmail) {
        // Lấy user từ email trong SecurityContext
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra quyền sở hữu booking
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZE, "Bạn không thể hủy booking này.");
        }

        // Kiểm tra đã thanh toán chưa
        if (Boolean.TRUE.equals(booking.getIsPaid())) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Booking đã thanh toán, không thể hủy.");
        }

        // Kiểm tra thời gian hiện tại so với check-in
        if (LocalDateTime.now().isAfter(booking.getCheckInDate())) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "Đã đến hoặc qua thời gian check-in, không thể hủy.");
        }

        // Thực hiện hủy
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }


}
