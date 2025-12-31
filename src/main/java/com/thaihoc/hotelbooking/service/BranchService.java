package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BranchMinPriceResponse;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeSummaryResponse;
import com.thaihoc.hotelbooking.entity.*;
import com.thaihoc.hotelbooking.enums.BranchStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import com.thaihoc.hotelbooking.repository.RoomPhotoRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchMapper branchMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomPhotoRepository roomPhotoRepository;

    @Autowired
    private RoomTypeBookingTypePriceRepository roomTypeBookingTypePriceRepository;

    @Autowired
    private RoomAvailabilityService roomAvailabilityService;


    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public BranchResponse create(BranchCreationRequest request, MultipartFile photo) {

        if (branchRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.BRANCH_EMAIL_ALREADY_EXISTS);
        }

        if (branchRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.BRANCH_PHONE_ALREADY_EXISTS);
        }

        if (branchRepository.existsByBranchName(request.getBranchName())) {
            throw new AppException(ErrorCode.BRANCH_NAME_ALREADY_EXISTS);
        }

        Branch branch = branchMapper.toBranch(request);

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo, "branches/");
            branch.setPhotoUrl(photoUrl);
        }

        Branch saved = branchRepository.save(branch);
        return branchMapper.toBranchResponse(saved);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public String delete(String id) {
        if (branchRepository.existsById(id)) {
            branchRepository.deleteById(id);
            return "Branch has been deleted";
        }
        return "Branch not found";
    }

    public List<Branch> getAll() {
        return branchRepository.findAll();
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public BranchResponse update(String id, BranchUpdateRequest request, MultipartFile photo) {
        Branch  branch = branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND));
        branchMapper.updateBranch(branch, request);

        if (photo != null && !photo.isEmpty()) {
            String newPhotoUrl =  fileStorageService.update(photo, branch.getPhotoUrl(), "branches/");
            branch.setPhotoUrl(newPhotoUrl);
        }

        Branch saved = branchRepository.save(branch);

        return branchMapper.toBranchResponse(saved);
    }

    public BranchResponse getById(String id) {
        return branchMapper.toBranchResponse(branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND)));
    }

    public PageResponse<BranchResponse> getAllPaging(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Branch> branchPage;

        if (search != null && !search.isBlank()) {
            branchPage = branchRepository.searchByNameOrAddress(search, pageable);
        } else {
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchResponse> responses = branchPage.getContent()
                .stream()
                .map(branchMapper::toBranchResponse)
                .toList();

        return PageResponse.<BranchResponse>builder()
                .page(page)
                .size(size)
                .totalElements(branchPage.getTotalElements())
                .totalPages(branchPage.getTotalPages())
                .items(responses)
                .build();
    }

    public BranchResponse updateStatus(String id, BranchStatus status) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        branch.setStatus(status);
        Branch saved = branchRepository.save(branch);

        return branchMapper.toBranchResponse(saved);
    }

    public List<BranchMinPriceResponse> searchHotels(
            String bookingTypeCode,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hours,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        // 1) Lấy candidate theo bookingType + location (đều optional)
        List<RoomTypeBookingTypePrice> candidates =
                roomTypeBookingTypePriceRepository.searchCandidates(
                        normalizeNullable(bookingTypeCode),
                        normalizeNullable(location)
                );

        if (candidates.isEmpty()) return List.of();

        // 2) Chuẩn hoá checkOut nếu chỉ truyền checkIn + hours
        LocalDateTime normalizedCheckIn = checkIn;
        LocalDateTime normalizedCheckOut = checkOut;

        if (normalizedCheckIn != null && normalizedCheckOut == null && hours != null && hours > 0) {
            normalizedCheckOut = normalizedCheckIn.plusHours(hours);
        }

        // 3) Map lưu “min price theo branch”
        Map<String, BranchMinPriceResponse> bestByBranch = new HashMap<>();

        for (RoomTypeBookingTypePrice priceCfg : candidates) {
            BookingType bt = priceCfg.getBookingType();
            Branch branch = priceCfg.getRoomType().getBranch();

            // 3.1) Nếu có checkIn/checkOut => validate theo bookingType tương ứng
            if (normalizedCheckIn != null && normalizedCheckOut != null) {
                validateBookingTime(normalizedCheckIn, normalizedCheckOut, bt);

                // nếu có hours thì check khớp (optional)
                if (hours != null) {
                    long diffHours = ChronoUnit.HOURS.between(normalizedCheckIn, normalizedCheckOut);
                    if (diffHours != hours.longValue()) {
                        // bạn có thể throw hoặc bỏ qua; mình chọn throw giống createBooking
                        throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
                    }
                }

                // 3.2) Check availability theo roomType
                boolean available = roomAvailabilityService.isRoomTypeAvailable(
                        priceCfg.getRoomType().getId(),
                        normalizedCheckIn,
                        normalizedCheckOut
                );
                if (!available) continue;
            }

            // 3.3) Tính giá cho config này
            BigDecimal computedPrice = computeSearchPrice(priceCfg, bt, normalizedCheckIn, normalizedCheckOut, hours);

            if (computedPrice == null) continue;

            // 3.4) Lọc khoảng giá (optional)
            if (minPrice != null && computedPrice.compareTo(minPrice) < 0) continue;
            if (maxPrice != null && computedPrice.compareTo(maxPrice) > 0) continue;

            // 3.5) Cập nhật min theo branch
            BranchMinPriceResponse currentBest = bestByBranch.get(branch.getId());
            if (currentBest == null || computedPrice.compareTo(currentBest.getMinPrice()) < 0) {
                bestByBranch.put(branch.getId(),
                        BranchMinPriceResponse.builder()
                                .branchId(branch.getId())
                                .branchName(branch.getBranchName())
                                .address(branch.getAddress())
                                .photoUrl(branch.getPhotoUrl())
                                .roomTypeId(priceCfg.getRoomType().getId())
                                .roomTypeName(priceCfg.getRoomType().getTypeName())
                                .bookingTypeCode(bt.getCode())
                                .minPrice(computedPrice)
                                .currency(priceCfg.getCurrency() != null ? priceCfg.getCurrency() : "VND")
                                .build()
                );
            }
        }

        // 4) Trả về list (sort giá tăng dần cho dễ xem)
        return bestByBranch.values().stream()
                .sorted(Comparator.comparing(BranchMinPriceResponse::getMinPrice))
                .toList();
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Validate tương tự validateBooking() bạn đang dùng trong BookingService
     * (mình giữ logic gần giống để tránh sai lệch)
     */
    private void validateBookingTime(LocalDateTime checkIn, LocalDateTime checkOut, BookingType bookingType) {
        if (checkOut.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        String code = bookingType.getCode();

        if ("HOUR".equals(code)) {
            // checkIn nằm trong khung giờ cho phép
            if (bookingType.getDefaultCheckInTime() != null && bookingType.getDefaultCheckOutTime() != null) {
                int h = checkIn.getHour();
                if (h < bookingType.getDefaultCheckInTime().getHour()
                        || h > bookingType.getDefaultCheckOutTime().getHour()) {
                    throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
                }
            }

            long hours = ChronoUnit.HOURS.between(checkIn, checkOut);

            // max 5h theo logic hiện tại của bạn (hoặc dùng bookingType.durationHours/maxHours tuỳ bạn)
            if (hours > 5) throw new AppException(ErrorCode.BOOKING_DATE_INVALID);

        } else if ("NIGHT".equals(code)) {
            if (checkIn.getHour() < 21 || checkOut.getHour() != 12) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }

        } else if ("DAY".equals(code)) {
            if (checkIn.getHour() != 14 || checkOut.getHour() != 12) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }
            if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
            }
        }
    }

    /**
     * Nếu có checkIn/checkOut => tính đúng theo calculateTotalPrice() của bạn.
     * Nếu không có thời gian => trả về "giá từ" (base + additional theo hours nếu HOUR).
     */
    private BigDecimal computeSearchPrice(
            RoomTypeBookingTypePrice priceCfg,
            BookingType bookingType,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hoursInput
    ) {
        BigDecimal basePrice = priceCfg.getPrice();
        if (basePrice == null) return null;

        // Có đủ thời gian => tính đúng
        if (checkIn != null && checkOut != null) {
            return calculateTotalPrice(priceCfg, checkIn, checkOut, bookingType);
        }

        // Không có thời gian => trả về starting price
        if ("HOUR".equals(bookingType.getCode())) {
            int h = (hoursInput != null && hoursInput > 0) ? hoursInput : 1;

            if (priceCfg.getMaxHours() != null && h > priceCfg.getMaxHours()) return null;
            if (h > 5) return null; // bám theo rule hiện tại

            BigDecimal total = basePrice;
            if (h > 1 && priceCfg.getAdditionalHourPrice() != null) {
                total = total.add(priceCfg.getAdditionalHourPrice().multiply(BigDecimal.valueOf(h - 1)));
            }
            return total;
        }

        // DAY / NIGHT
        return basePrice;
    }

    /**
     * Copy gần nguyên calculateTotalPrice() từ BookingService của bạn
     */
    private BigDecimal calculateTotalPrice(RoomTypeBookingTypePrice priceConfig,
                                           LocalDateTime checkIn, LocalDateTime checkOut,
                                           BookingType bookingType) {

        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        long hours = ChronoUnit.HOURS.between(checkIn, checkOut);

        if (days < 0 || (days == 0 && hours <= 0)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal basePrice = priceConfig.getPrice();

        if ("DAY".equals(bookingType.getCode())) {
            LocalTime requiredCheckIn = bookingType.getDefaultCheckInTime();
            LocalTime requiredCheckOut = bookingType.getDefaultCheckOutTime();

            if (requiredCheckIn != null && !checkIn.toLocalTime().equals(requiredCheckIn))
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);

            if (requiredCheckOut != null && !checkOut.toLocalTime().equals(requiredCheckOut))
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);

            if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate()))
                throw new AppException(ErrorCode.BOOKING_DATE_INVALID);

            LocalDate start = checkIn.toLocalDate();
            LocalDate endExclusive = checkOut.toLocalDate();

            LocalDate current = start;
            while (current.isBefore(endExclusive)) {
                BigDecimal daily = basePrice;

                if (current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    if (priceConfig.getWeekendSurcharge() != null)
                        daily = daily.add(priceConfig.getWeekendSurcharge());
                }

                totalPrice = totalPrice.add(daily);
                current = current.plusDays(1);
            }

            return totalPrice;

        } else if ("NIGHT".equals(bookingType.getCode())) {
            totalPrice = basePrice;
            if (checkIn.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    checkIn.getDayOfWeek() == DayOfWeek.SUNDAY) {
                if (priceConfig.getWeekendSurcharge() != null)
                    totalPrice = totalPrice.add(priceConfig.getWeekendSurcharge());
            }
            return totalPrice;

        } else if ("HOUR".equals(bookingType.getCode())) {
            if (hours > 5) return null;

            totalPrice = basePrice;

            if (checkIn.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    checkIn.getDayOfWeek() == DayOfWeek.SUNDAY) {
                if (priceConfig.getWeekendSurcharge() != null)
                    totalPrice = totalPrice.add(priceConfig.getWeekendSurcharge());
            }

            if (hours > 1) {
                long additionalHours = hours - 1;
                if (priceConfig.getAdditionalHourPrice() != null) {
                    totalPrice = totalPrice.add(
                            priceConfig.getAdditionalHourPrice().multiply(BigDecimal.valueOf(additionalHours))
                    );
                }
            }
            return totalPrice;
        }

        return null;
    }



}
