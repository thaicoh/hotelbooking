package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.BookingTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BookingTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BookingTypeResponse;
import com.thaihoc.hotelbooking.entity.BookingType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingTypeMapper {
    BookingType toBookingType(BookingTypeCreationRequest request);
    BookingTypeResponse toBookingTypeResponse(BookingType bookingType);
    void updateBookingType(@MappingTarget BookingType bookingType, BookingTypeUpdateRequest request);
}
