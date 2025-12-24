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


    public RoomPhotoResponse uploadMainPhoto(Long roomTypeId, MultipartFile photo) {
        // 1. Kiểm tra RoomType tồn tại
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        // 2. Kiểm tra đã có ảnh chính chưa
        boolean existsMain = roomPhotoRepository.existsByRoomTypeIdAndIsMainTrue(roomTypeId);
        if (existsMain) {
            throw new AppException(ErrorCode.ROOM_MAIN_PHOTO_ALREADY_EXISTS);
        }

        // 3. Lưu file vào storage
        String photoUrl = fileStorageService.store(photo, "rooms/");

        // 4. Tạo entity RoomPhoto
        RoomPhoto roomPhoto = RoomPhoto.builder()
                .photoUrl(photoUrl)
                .roomType(roomType)
                .isMain(true) // luôn là ảnh chính
                .build();

        // 5. Lưu DB
        RoomPhoto saved = roomPhotoRepository.save(roomPhoto);

        // 6. Convert sang response
        return roomPhotoMapper.toRoomPhotoResponse(saved);
    }


    public RoomPhotoResponse updateMainPhoto(Long roomTypeId, MultipartFile photo) {
        // 1. Kiểm tra RoomType tồn tại
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        // 2. Tìm ảnh chính hiện tại
        RoomPhoto mainPhoto = roomPhotoRepository.findByRoomTypeIdAndIsMainTrue(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_MAIN_PHOTO_NOT_FOUND));

        // 3. Xóa file cũ
        fileStorageService.delete(mainPhoto.getPhotoUrl());

        // 4. Lưu file mới
        String newPhotoUrl = fileStorageService.store(photo, "rooms/");

        // 5. Cập nhật entity
        mainPhoto.setPhotoUrl(newPhotoUrl);

        RoomPhoto saved = roomPhotoRepository.save(mainPhoto);

        // 6. Convert sang response
        return roomPhotoMapper.toRoomPhotoResponse(saved);
    }

    public RoomPhotoResponse updateRoomPhoto(Long photoId, MultipartFile photo) {
        // 1. Tìm ảnh theo id
        RoomPhoto roomPhoto = roomPhotoRepository.findById(photoId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_PHOTO_NOT_FOUND));

        // 2. Kiểm tra: nếu là ảnh chính thì báo lỗi
        if (Boolean.TRUE.equals(roomPhoto.getIsMain())) {
            throw new AppException(ErrorCode.ROOM_PHOTO_IS_MAIN);
            // hoặc tạo ErrorCode riêng: "Không thể update ảnh chính bằng API này"
        }

        // 3. Xóa file cũ
        fileStorageService.delete(roomPhoto.getPhotoUrl());

        // 4. Lưu file mới
        String newPhotoUrl = fileStorageService.store(photo, "rooms/");

        // 5. Cập nhật entity
        roomPhoto.setPhotoUrl(newPhotoUrl);

        RoomPhoto saved = roomPhotoRepository.save(roomPhoto);

        // 6. Convert sang response
        return roomPhotoMapper.toRoomPhotoResponse(saved);
    }


    public RoomPhotoResponse uploadSubPhoto(Long roomTypeId, MultipartFile photo) {
        // 1. Kiểm tra RoomType tồn tại
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        // 2. Lưu file vào storage
        String photoUrl = fileStorageService.store(photo, "rooms/");

        // 3. Tạo entity RoomPhoto (luôn là ảnh phụ)
        RoomPhoto roomPhoto = RoomPhoto.builder()
                .photoUrl(photoUrl)
                .roomType(roomType)
                .isMain(false) // ảnh phụ
                .build();

        // 4. Lưu DB
        RoomPhoto saved = roomPhotoRepository.save(roomPhoto);

        // 5. Convert sang response
        return roomPhotoMapper.toRoomPhotoResponse(saved);
    }


}
