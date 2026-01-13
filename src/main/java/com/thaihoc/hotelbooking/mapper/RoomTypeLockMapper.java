package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.RoomTypeLockRequest;
import com.thaihoc.hotelbooking.dto.response.RoomTypeLockResponse;
import com.thaihoc.hotelbooking.entity.RoomTypeLock;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {BranchMapper.class})
public interface RoomTypeLockMapper {
    RoomTypeLock toRoomTypeLock(RoomTypeLockRequest request);
    RoomTypeLockResponse toRoomTypeLockResponse(RoomTypeLock roomTypeLock);

    List<RoomTypeLockResponse> toRoomTypeLockResponseList(List<RoomTypeLock> roomTypeLocks);


}
