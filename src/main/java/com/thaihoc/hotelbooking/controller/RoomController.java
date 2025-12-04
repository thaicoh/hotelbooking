package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomUpdateRequest;
import com.thaihoc.hotelbooking.dto.request.StatusUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomResponse;
import com.thaihoc.hotelbooking.entity.Room;
import com.thaihoc.hotelbooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    RoomService roomService;

    @PostMapping
    private ApiResponse<RoomResponse> create(@RequestBody RoomCreationRequest request){
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.createRoom(request))
                .build();
    }

    @GetMapping
    private ApiResponse<List<RoomResponse>> getRooms(){
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomService.getAll())
                .build();
    }

    @DeleteMapping("/{id}")
    private ApiResponse<String> delete(@PathVariable String id){
        return ApiResponse.<String>builder()
                .result(roomService.deleteRoom(id))
                .build();
    }

    @PutMapping("/{id}")
    private ApiResponse<RoomResponse> update(@PathVariable String id, @RequestBody RoomUpdateRequest request){
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.update(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    private ApiResponse<RoomResponse> updateRoomStatus(@PathVariable String id, @RequestBody StatusUpdateRequest request){
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.setRoomStatus(request, id))
                .build();
    }

    @GetMapping("/{id}")
    private ApiResponse<RoomResponse> getRoom(@PathVariable String id){
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.getRoom(id))
                .build();
    }

}
