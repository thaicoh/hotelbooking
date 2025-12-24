package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.RoomPhotoCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomPhotoResponse;
import com.thaihoc.hotelbooking.service.RoomPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/room_photo")
public class RoomPhotoController {
    @Autowired
    RoomPhotoService roomPhotoService;

    @PostMapping
    private ApiResponse<List<RoomPhotoResponse>> CreateRoomPhotos(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("mainPhotoIndex") Integer mainPhotoIndex,
            @RequestParam("photos") List<MultipartFile> photos
    ){
        RoomPhotoCreationRequest request = RoomPhotoCreationRequest.builder()
                .roomTypeId(roomTypeId)
                .mainPhotoIndex(mainPhotoIndex)
                .photos(photos)
                .build();

        return ApiResponse.<List<RoomPhotoResponse>>builder()
                .result(roomPhotoService.createRoomPhotos(request))
                .build();
    }

    @DeleteMapping("/{id}")
    private ApiResponse<String> DeleteRoomPhoto(@PathVariable Long id){
        return ApiResponse.<String>builder()
                .result(roomPhotoService.deleteRoomPhoto(id))
                .build();
    }



    @GetMapping("/{roomTypeId}")
    private ApiResponse<List<RoomPhotoResponse>> GetRoomPhotosByRoomTypeId(@PathVariable Long roomTypeId){
        return ApiResponse.<List<RoomPhotoResponse>>builder()
                .result(roomPhotoService.getRoomPhotosByRoomTypeId(roomTypeId))
                .build();
    }
    @PostMapping("/main")
    private ApiResponse<RoomPhotoResponse> uploadMainPhoto(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("photo") MultipartFile photo
    ) {
        return ApiResponse.<RoomPhotoResponse>builder()
                .result(roomPhotoService.uploadMainPhoto(roomTypeId, photo))
                .build();
    }

    @PutMapping("/main")
    private ApiResponse<RoomPhotoResponse> updateMainPhoto(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("photo") MultipartFile photo
    ) {
        return ApiResponse.<RoomPhotoResponse>builder()
                .result(roomPhotoService.updateMainPhoto(roomTypeId, photo))
                .build();
    }


    @PutMapping("/{photoId}")
    private ApiResponse<RoomPhotoResponse> updateRoomPhoto(
            @PathVariable Long photoId,
            @RequestParam("photo") MultipartFile photo
    ) {
        return ApiResponse.<RoomPhotoResponse>builder()
                .result(roomPhotoService.updateRoomPhoto(photoId, photo))
                .build();
    }

    @PostMapping("/sub")
    private ApiResponse<RoomPhotoResponse> uploadSubPhoto(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("photo") MultipartFile photo
    ) {
        return ApiResponse.<RoomPhotoResponse>builder()
                .result(roomPhotoService.uploadSubPhoto(roomTypeId, photo))
                .build();
    }

}
