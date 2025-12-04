package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.RoomCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomResponse;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.repository.RoomRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    Room toRoom(RoomCreationRequest roomCreationRequest);

    RoomResponse  toRoomResponse(Room room);

    List<RoomResponse> toRoomResponseList(List<Room> rooms);

    void updateRoom(@MappingTarget Room room, RoomUpdateRequest roomUpdateRequest);
}
