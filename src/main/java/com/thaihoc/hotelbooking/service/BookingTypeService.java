package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BookingTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BookingTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BookingTypeResponse;
import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BookingTypeMapper;
import com.thaihoc.hotelbooking.repository.BookingTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingTypeService {
    @Autowired
    private BookingTypeRepository bookingTypeRepository;

    @Autowired
    private BookingTypeMapper bookingTypeMapper;

    // Tạo mới BookingType
    public BookingTypeResponse create(BookingTypeCreationRequest request) {
        if (bookingTypeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.BOOKING_TYPE_CODE_ALREADY_EXISTS);
        }

        BookingType bookingType = bookingTypeMapper.toBookingType(request);
        BookingType savedBookingType = bookingTypeRepository.save(bookingType);

        return bookingTypeMapper.toBookingTypeResponse(savedBookingType);
    }

    // Lấy tất cả BookingType
    public List<BookingTypeResponse> findAll() {
        return bookingTypeRepository.findAll()
                .stream()
                .map(bookingTypeMapper::toBookingTypeResponse)
                .collect(Collectors.toList());
    }

    // Lấy BookingType theo id
    public BookingTypeResponse findById(Long id) {
        BookingType bookingType = bookingTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));
        return bookingTypeMapper.toBookingTypeResponse(bookingType);
    }

    // Cập nhật BookingType
    public BookingTypeResponse updateBookingType(Long id, BookingTypeUpdateRequest request) {
        BookingType bookingType = bookingTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));

        bookingTypeMapper.updateBookingType(bookingType, request);

        BookingType savedBookingType = bookingTypeRepository.save(bookingType);
        return bookingTypeMapper.toBookingTypeResponse(savedBookingType);
    }

    // Xóa BookingType
    @Transactional
    public String deleteById(Long id) {
        if (!bookingTypeRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND);
        }

        bookingTypeRepository.deleteById(id);
        return "BookingType deleted successfully";
    }


}
