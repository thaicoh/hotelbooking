package com.thaihoc.hotelbooking.mapper;


import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeBookingTypePriceUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomTypeBookingTypePriceResponse;
import com.thaihoc.hotelbooking.entity.RoomTypeBookingTypePrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomTypeBookingTypePriceMapper {

    // Chuyển từ CreationRequest -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomType", ignore = true)     // set trong service
    @Mapping(target = "bookingType", ignore = true)  // set trong service
    RoomTypeBookingTypePrice toEntity(RoomTypeBookingTypePriceCreationRequest request);

    // Chuyển từ Entity -> Response
    @Mapping(source = "roomType.id", target = "roomTypeId")
    @Mapping(source = "bookingType.id", target = "bookingTypeId")
    RoomTypeBookingTypePriceResponse toResponse(RoomTypeBookingTypePrice entity);

    // Cập nhật entity từ UpdateRequest
    void updateEntity(@MappingTarget RoomTypeBookingTypePrice entity,
                      RoomTypeBookingTypePriceUpdateRequest request);
}

