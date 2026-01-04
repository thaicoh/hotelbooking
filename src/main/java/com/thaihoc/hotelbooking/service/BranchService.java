package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.*;
import com.thaihoc.hotelbooking.entity.*;
import com.thaihoc.hotelbooking.enums.BranchStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import com.thaihoc.hotelbooking.repository.RoomPhotoRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import com.thaihoc.hotelbooking.util.BookingTimeUtil;
import com.thaihoc.hotelbooking.util.PriceCalculatorUtil;
import com.thaihoc.hotelbooking.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
                        StringUtil.normalizeNullable(bookingTypeCode),
                        StringUtil.normalizeNullable(location)
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
                BookingTimeUtil.validateBookingTime(normalizedCheckIn, normalizedCheckOut, bt);

                // nếu có hours thì check khớp (optional)
                if ("HOUR".equals(bookingTypeCode) && hours != null) {
                    long diffHours = ChronoUnit.HOURS.between(normalizedCheckIn, normalizedCheckOut);
                    if (diffHours != hours.longValue()) {
                        throw new AppException(ErrorCode.BOOKING_DATE_INVALID,
                                "Số giờ truyền vào (" + hours + ") không khớp với khoảng thời gian thực tế (" + diffHours + ").");
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
            BigDecimal computedPrice = PriceCalculatorUtil.computeSearchPrice(priceCfg, bt, normalizedCheckIn, normalizedCheckOut, hours);

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



    public BranchDetailResponse getHotelDetail(
            String branchId,
            String bookingTypeCode,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Integer hours
    ) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found"));

        // Chuẩn hoá checkOut nếu chỉ truyền checkIn + hours
        LocalDateTime normalizedCheckIn = checkIn;
        LocalDateTime normalizedCheckOut = checkOut;
        if (normalizedCheckIn != null && normalizedCheckOut == null && hours != null && hours > 0) {
            normalizedCheckOut = normalizedCheckIn.plusHours(hours);
        }

        List<RoomType> roomTypes = roomTypeRepository.findByBranchId(branchId);

        List<RoomTypeDetailResponse> roomResponses = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            // Lấy config giá theo bookingType
            RoomTypeBookingTypePrice priceCfg = roomTypeBookingTypePriceRepository
                    .findByRoomTypeIdAndBookingTypeCode(roomType.getId(), bookingTypeCode)
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND, "Không tìm thấy cấu hình giá cho roomTypeId=" + roomType.getId()));

            if (priceCfg == null) continue;

            // Validate thời gian đặt phòng
            if (normalizedCheckIn != null && normalizedCheckOut != null) {
                BookingTimeUtil.validateBookingTime(normalizedCheckIn, normalizedCheckOut, priceCfg.getBookingType());
                boolean available = roomAvailabilityService.isRoomTypeAvailable(
                        roomType.getId(), normalizedCheckIn, normalizedCheckOut);
                if (!available) continue;
            }

            // Tính giá
            BigDecimal computedPrice = PriceCalculatorUtil.computeSearchPrice(
                    priceCfg,
                    priceCfg.getBookingType(),
                    normalizedCheckIn,
                    normalizedCheckOut,
                    hours
            );


            if (computedPrice == null) continue;


            // Lấy số lượng phòng còn trống
            int availableRooms = roomAvailabilityService.countAvailableRooms(
                    roomType.getId(), normalizedCheckIn, normalizedCheckOut);

            List<RoomPhoto> photos = roomPhotoRepository.findByRoomTypeId(roomType.getId());

            roomResponses.add(RoomTypeDetailResponse.builder()
                    .roomTypeId(roomType.getId())
                    .roomTypeName(roomType.getTypeName())
                    .capacity(roomType.getCapacity())
                    .description(roomType.getDescription())
                    .price(computedPrice)
                    .currency(priceCfg.getCurrency() != null ? priceCfg.getCurrency() : "VND")
                    .availableRooms(availableRooms)
                    .photoUrls(
                            photos.stream()
                                    .map(RoomPhoto::getPhotoUrl)
                                    .toList()
                    )

                    .build());
        }

        return BranchDetailResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .photoUrl(branch.getPhotoUrl())
                .rooms(roomResponses)
                .build();
    }

}
