package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.BookingCreationRequest;
import com.thaihoc.hotelbooking.dto.response.BookingResponse;
import com.thaihoc.hotelbooking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "roomType.id", target = "roomTypeId")
    @Mapping(source = "bookingType.id", target = "bookingTypeId")
    @Mapping(source = "room.roomId", target = "roomId")
    BookingResponse toResponse(Booking booking);

    Booking toEntity(BookingCreationRequest request);

}
