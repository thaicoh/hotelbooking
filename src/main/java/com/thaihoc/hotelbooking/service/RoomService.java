package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.RoomCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomUpdateRequest;
import com.thaihoc.hotelbooking.dto.request.StatusUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomResponse;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.RoomMapper;
import com.thaihoc.hotelbooking.repository.RoomRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoomMapper roomMapper;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    public RoomResponse createRoom(RoomCreationRequest  roomCreationRequest) {

        RoomType roomType = roomTypeRepository.findById(roomCreationRequest.getRoomTypeId())
                .orElseThrow(() ->  new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        if(roomRepository.existsByRoomNumber(roomCreationRequest.getRoomNumber())) throw new AppException(ErrorCode.ROOM_NUMBER_ALREADY_EXISTS);

        Room room = roomMapper.toRoom(roomCreationRequest);

        room.setRoomType(roomType);

        Room saved = roomRepository.save(room);

        return  roomMapper.toRoomResponse(saved);
    }

    public List<RoomResponse> getAll() {
        List<Room> rooms = roomRepository.findAll();
        return roomMapper.toRoomResponseList(rooms);
    }

    public String deleteRoom(String id) {
        Room room = roomRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.ROOM_NOT_FOUND));

        try {
            roomRepository.delete(room);
        }catch (Exception e){
            throw new AppException(ErrorCode.ROOM_DELETE_FAILED);
        }

        return "Room has been deleted";
    }

    public RoomResponse update(String id, RoomUpdateRequest roomUpdateRequest){

        Room room = roomRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.ROOM_NOT_FOUND));

        RoomType roomType = roomTypeRepository.findById(roomUpdateRequest.getRoomTypeId())
                .orElseThrow(() ->  new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        try {

            roomMapper.updateRoom(room, roomUpdateRequest);
            room.setRoomType(roomType);
            Room saved = roomRepository.save(room);
            return  roomMapper.toRoomResponse(saved);

        }catch (Exception e){
            throw new AppException(ErrorCode.ROOM_UPDATE_FAILED);
        }
    }

    public RoomResponse setRoomStatus(StatusUpdateRequest request, String id) {

        // "Available", "Occupied", "Maintenance"
        Room room =  roomRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.ROOM_NOT_FOUND));

        String status = request.getStatus();

        if(status.equals("Available") || status.equals("Occupied") || status.equals("Maintenance")){

            room.setStatus(status);
            Room saved = roomRepository.save(room);
            return  roomMapper.toRoomResponse(saved);

        }else {
            throw new AppException(ErrorCode.ROOM_STATUS_INVALID);
        }
    }

    public RoomResponse getRoom(String id) {
        return roomMapper.toRoomResponse(roomRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.ROOM_NOT_FOUND)));
    }
}
