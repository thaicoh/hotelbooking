package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomTypeBookingTypePriceResponse;
import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.RoomTypeBookingTypePriceMapper;
import com.thaihoc.hotelbooking.repository.BookingTypeRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeBookingTypePriceService {
    @Autowired
    private RoomTypeBookingTypePriceRepository priceRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private BookingTypeRepository bookingTypeRepository;

    @Autowired
    private RoomTypeBookingTypePriceMapper priceMapper;

    // Tạo mới
    public RoomTypeBookingTypePriceResponse create(RoomTypeBookingTypePriceCreationRequest request) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        BookingType bookingType = bookingTypeRepository.findById(request.getBookingTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));

        RoomTypeBookingTypePrice price = priceMapper.toEntity(request);
        price.setRoomType(roomType);
        price.setBookingType(bookingType);

        RoomTypeBookingTypePrice saved = priceRepository.save(price);
        return priceMapper.toResponse(saved);
    }

    // Lấy tất cả
    public List<RoomTypeBookingTypePriceResponse> findAll() {
        return priceRepository.findAll()
                .stream()
                .map(priceMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy theo id
    public RoomTypeBookingTypePriceResponse findById(Long id) {
        RoomTypeBookingTypePrice price = priceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND));
        return priceMapper.toResponse(price);
    }

    // Cập nhật
    public RoomTypeBookingTypePriceResponse update(Long id, RoomTypeBookingTypePriceUpdateRequest request) {
        RoomTypeBookingTypePrice price = priceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND));

        priceMapper.updateEntity(price, request);

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
            price.setRoomType(roomType);
        }

        if (request.getBookingTypeId() != null) {
            BookingType bookingType = bookingTypeRepository.findById(request.getBookingTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));
            price.setBookingType(bookingType);
        }

        RoomTypeBookingTypePrice saved = priceRepository.save(price);
        return priceMapper.toResponse(saved);
    }

    // Xóa
    @Transactional
    public String deleteById(Long id) {
        if (!priceRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND);
        }
        priceRepository.deleteById(id);
        return "RoomTypeBookingTypePrice deleted successfully";
    }

    public List<RoomTypeBookingTypePriceResponse> findByRoomTypeId(Long roomTypeId) {
        List<RoomTypeBookingTypePrice> prices = priceRepository.findByRoomType_Id(roomTypeId);
        if (prices.isEmpty()) {
            throw new AppException(ErrorCode.ROOM_TYPE_BOOKING_TYPE_PRICE_NOT_FOUND);
        }
        return prices.stream()
                .map(priceMapper::toResponse)
                .collect(Collectors.toList());
    }


}
