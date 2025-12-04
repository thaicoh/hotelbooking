package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.RoomPhotoCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.RoomPhotoResponse;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.RoomPhotoMapper;
import com.thaihoc.hotelbooking.repository.RoomPhotoRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomPhotoService {
    @Autowired
    RoomPhotoRepository roomPhotoRepository;

    @Autowired
    RoomPhotoMapper roomPhotoMapper;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Autowired
    FileStorageService fileStorageService;


    public List<RoomPhotoResponse> createRoomPhotos(RoomPhotoCreationRequest request) {
        // 1. Kiểm tra RoomType tồn tại
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        List<RoomPhotoResponse> responses = new ArrayList<>();

        // 2. Lặp qua danh sách ảnh
        List<MultipartFile> photos = request.getPhotos();
        for (int i = 0; i < photos.size(); i++) {
            MultipartFile photo = photos.get(i);

            // 3. Lưu file vào storage
            String photoUrl = fileStorageService.store(photo, "rooms/");

            RoomPhoto roomPhoto = RoomPhoto.builder()
                    .photoUrl(photoUrl)
                    .roomType(roomType)
                    .isMain(i == request.getMainPhotoIndex())
                    .build();

            // 5. Lưu vào DB
            RoomPhoto saved = roomPhotoRepository.save(roomPhoto);

            // 6. Convert sang response
            responses.add(roomPhotoMapper.toRoomPhotoResponse(saved));
        }

        return responses;
    }

    @Transactional
    public void deleteByRoomTypeId(Long roomTypeId) {
        List<RoomPhoto> photos = roomPhotoRepository.findByRoomTypeId(roomTypeId);

        for (RoomPhoto photo : photos) {
            fileStorageService.delete(photo.getPhotoUrl());
        }

        roomPhotoRepository.deleteByRoomTypeId(roomTypeId);
    }

    public String deleteRoomPhoto(Long roomPhotoId) {
        RoomPhoto photo = roomPhotoRepository.findById(roomPhotoId).orElseThrow(() -> new AppException(ErrorCode.ROOM_PHOTO_NOT_FOUND));

        try {
            roomPhotoRepository.delete(photo);
            fileStorageService.delete(photo.getPhotoUrl());

        }catch (Exception e) {
            throw new AppException(ErrorCode.ROOM_PHOTO_DELETE_FAILED);
        }

        return "Room Photo has been deleted successfully";
    }

    public List<RoomPhotoResponse> getRoomPhotosByRoomTypeId(Long roomTypeId) {

        if (!roomTypeRepository.existsById(roomTypeId)) throw new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND);

        List<RoomPhoto> photos = roomPhotoRepository.findByRoomTypeId(roomTypeId);

        List<RoomPhotoResponse> responses = new ArrayList<>();

        for (RoomPhoto photo : photos) {
            responses.add(roomPhotoMapper.toRoomPhotoResponse(photo));
        }

        return responses;
    }

}
