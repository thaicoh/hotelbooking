package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.BookingListItemResponse;
import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.entity.*;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BookingMapper;
import com.thaihoc.hotelbooking.repository.*;
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


    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public BookingResponse createBooking(BookingCreationRequest request) {

        log.info("Create booking request: roomTypeCode={}, bookingTypeId={}, checkIn={}, checkOut={}, paymentMethod={}",
                request.getRoomTypeId(), request.getBookingTypeCode(),
                request.getCheckInDate(), request.getCheckOutDate(),
                request.getPaymentMethod());


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        BookingType bookingType = bookingTypeRepository.findByCode(request.getBookingTypeCode())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));

        validateBooking(request, bookingType);

        // ✅ Kiểm tra phòng trống trước khi tạo booking
        boolean available = roomAvailabilityService.isRoomTypeAvailable(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!available) {
            throw new AppException(ErrorCode.BOOKING_ROOM_NOT_AVAILABLE);
        }

        RoomTypeBookingTypePrice priceConfig = roomTypeBookingTypePriceRepository
                .findByRoomType_IdAndBookingType_IdAndIsActive(
                        request.getRoomTypeId(),
                        bookingType.getId(),
                        true
                )
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND));

        // Tính tồng tiền
        BigDecimal totalPrice = calculateTotalPrice(priceConfig, request.getCheckInDate(), request.getCheckOutDate(), bookingType);

        // Dùng mapper để convert request -> entity
        Booking booking = bookingMapper.toEntity(request);

        booking.setUser(user);
        booking.setRoomType(roomType);
        booking.setBookingType(priceConfig.getBookingType());
        booking.setTotalPrice(totalPrice);
        booking.setIsPaid(false);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setCreatedBy(user.getFullName());
        booking.setBookingReference(UUID.randomUUID().toString());

        // ✅ Phân nhánh theo phương thức thanh toán
        if ("PAY_AT_HOTEL".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("RESERVED");
        } else if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("PENDING");
        } else {
            throw new AppException(ErrorCode.BOOKING_PAYMENT_METHOD_INVALID);
        }
        log.info("Booking created: bookingReference={}, status={}, totalPrice={}, user={}",
                booking.getBookingReference(), booking.getStatus(),
                booking.getTotalPrice(), booking.getUser().getEmail());



        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
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
                    .status(booking.getStatus())
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


    private BigDecimal calculateTotalPrice(RoomTypeBookingTypePrice priceConfig,
                                           LocalDateTime checkIn, LocalDateTime checkOut,
                                           BookingType bookingType) {

        // Kiểm tra ngày giờ nhập vào có hợp lệ không
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        long hours = ChronoUnit.HOURS.between(checkIn, checkOut);

        if (days < 0 || (days == 0 && hours <= 0)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal basePrice = priceConfig.getPrice();  // Giá phòng cơ bản

        // Kiểm tra loại booking (theo ngày, theo giờ, theo đêm)
        if ("DAY".equals(bookingType.getCode())) {

            LocalTime requiredCheckIn = bookingType.getDefaultCheckInTime();
            LocalTime requiredCheckOut = bookingType.getDefaultCheckOutTime();

            // 1. Validate giờ check-in phải khớp 100%
            if (!checkIn.toLocalTime().equals(requiredCheckIn)) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

            // 2. Validate giờ check-out phải khớp 100%
            if (!checkOut.toLocalTime().equals(requiredCheckOut)) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

            // 3. Ngày checkout phải sau ngày checkin
            if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

            // 4. Tính số ngày theo start inclusive – end exclusive
            LocalDate start = checkIn.toLocalDate();
            LocalDate endExclusive = checkOut.toLocalDate();

            LocalDate current = start;
            while (current.isBefore(endExclusive)) {
                BigDecimal daily = basePrice;

                // phụ phí cuối tuần
                if (current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    daily = daily.add(priceConfig.getWeekendSurcharge());
                }

                totalPrice = totalPrice.add(daily);
                current = current.plusDays(1);
            }

            return totalPrice;

        } else if (bookingType.getCode().equals("NIGHT")) {
            // Nếu là phòng đêm, tính giá cho 1 đêm
            totalPrice = basePrice;

            // Phụ phí cuối tuần (nếu có)
            if (checkIn.getDayOfWeek() == DayOfWeek.SATURDAY || checkIn.getDayOfWeek() == DayOfWeek.SUNDAY) {
                totalPrice = totalPrice.add(priceConfig.getWeekendSurcharge());
            }
        } else if (bookingType.getCode().equals("HOUR")) {
            // Nếu là phòng theo giờ, tính theo số giờ
            if (hours > 5) {
                throw new RuntimeException("Booking duration exceeds maximum allowed hours for hourly bookings.");
            }

            // Giá cho giờ đầu tiên (sử dụng basePrice đã cộng phụ phí cuối tuần nếu có)
            totalPrice = basePrice;

            // Phụ phí cuối tuần (nếu có) chỉ cộng vào giá cơ bản
            if (checkIn.getDayOfWeek() == DayOfWeek.SATURDAY || checkIn.getDayOfWeek() == DayOfWeek.SUNDAY) {
                totalPrice = totalPrice.add(priceConfig.getWeekendSurcharge());
            }

            // Tính giá cho các giờ tiếp theo (sử dụng additionalHourPrice)
            if (hours > 1) {
                long additionalHours = hours - 1; // Tính các giờ vượt quá
                totalPrice = totalPrice.add(priceConfig.getAdditionalHourPrice().multiply(BigDecimal.valueOf(additionalHours)));
            }

        }

        // Trả về tổng giá tiền
        return totalPrice;
    }

    private void validateBooking(BookingCreationRequest request, BookingType bookingType) {
        // Kiểm tra check-out không được trước thời điểm hiện tại
        if (request.getCheckOutDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        // Kiểm tra loại phòng và validate theo từng loại booking
        if (bookingType.getCode().equals("HOUR")) {
            // Kiểm tra phòng giờ: check-in và check-out phải nằm trong khoảng thời gian default check-in và check-out
            if (request.getCheckInDate().getHour() < bookingType.getDefaultCheckInTime().getHour() ||
                    request.getCheckInDate().getHour() > bookingType.getDefaultCheckOutTime().getHour()) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

            // Kiểm tra phòng giờ không quá 5 giờ
            long hours = ChronoUnit.HOURS.between(request.getCheckInDate(), request.getCheckOutDate());
            if (hours > 5) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

        } else if (bookingType.getCode().equals("NIGHT")) {
            // Kiểm tra phòng đêm: check-in phải từ 21h và check-out phải trước 12h trưa hôm sau
            if (request.getCheckInDate().getHour() < 21 || request.getCheckOutDate().getHour() != 12) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID );
            }
        } else if (bookingType.getCode().equals("DAY")) {
            // Kiểm tra phòng ngày: check-in phải lúc 14h và check-out phải trước 12h trưa
            if (request.getCheckInDate().getHour() != 14 || request.getCheckOutDate().getHour() != 12) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

            // Kiểm tra ngày checkout không được trước ngày checkin
            if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }
        }
    }






}
