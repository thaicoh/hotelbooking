package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.RoomTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomTypeResponse;
import com.thaihoc.hotelbooking.entity.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {BranchMapper.class})
public interface RoomTypeMapper {

    RoomType toRoomType(RoomTypeCreationRequest request);

    RoomTypeResponse toRoomTypeResponse(RoomType roomType);

    RoomType roomTypeUpdateRequesttoRoomType(RoomTypeUpdateRequest request);

    void updateRoomType(@MappingTarget RoomType roomType, RoomTypeUpdateRequest request);
}
