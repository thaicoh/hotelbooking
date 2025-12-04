package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.RoomPhotoCreationRequest;
import com.thaihoc.hotelbooking.dto.response.RoomPhotoResponse;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomPhotoMapper {
    RoomPhoto toRoomPhoto(RoomPhotoCreationRequest roomPhotoCreationRequest);

    RoomPhotoResponse  toRoomPhotoResponse(RoomPhoto  roomPhoto);
}