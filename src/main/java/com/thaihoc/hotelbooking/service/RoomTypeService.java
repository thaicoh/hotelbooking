package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.RoomTypeCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoomTypeUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.RoomTypeResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeSummaryResponse;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.mapper.RoomTypeMapper;
import com.thaihoc.hotelbooking.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeService {

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    RoomTypeMapper roomTypeMapper;

    @Autowired
    BranchMapper branchMapper;

    @Autowired
    RoomPhotoService roomPhotoService;

    @Autowired
    RoomPhotoRepository roomPhotoRepository;

    @Autowired
    private RoomTypeBookingTypePriceRepository priceRepository;


    public RoomTypeResponse create(RoomTypeCreationRequest request) {

        if (roomTypeRepository.existsByTypeName(request.getTypeName()))
            throw new AppException(ErrorCode.ROOM_TYPE_NAME_ALREADY_EXISTS);

        if (!branchRepository.existsById(request.getBranchId())) throw new AppException(ErrorCode.BRANCH_NOT_FOUND);

        RoomType roomType = roomTypeMapper.toRoomType(request);

        roomType.setBranch(branchRepository.findById(request.getBranchId()).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_CREATION_FAILED)));

        RoomType savedRoomType = roomTypeRepository.save(roomType);

        RoomTypeResponse roomTypeResponse = roomTypeMapper.toRoomTypeResponse(savedRoomType);

        roomTypeResponse.setBranch(
                branchMapper.toBranchResponse(branchRepository.findById(request.getBranchId()).orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND)))
        );
         return roomTypeResponse;
    }


    public List<RoomTypeResponse> findAll() {
            return roomTypeRepository.findAll()
                    .stream()
                    .map(roomTypeMapper::toRoomTypeResponse)
                    .collect(Collectors.toList());
    }

    @Transactional
    public String deleteById(Long id) {
        if(!roomTypeRepository.existsById(id)) throw new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND);

        if(roomPhotoRepository.existsByRoomTypeId(id)){

            roomPhotoService.deleteByRoomTypeId(id);
        }

        roomTypeRepository.deleteById(id);

        return "roomType deleted successfully";
    }

    public RoomTypeResponse updateRoomType( Long id, RoomTypeUpdateRequest request) {

        RoomType roomType = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        roomTypeMapper.updateRoomType(roomType, request);

        RoomType savedRoomType = roomTypeRepository.save(roomType);

        return roomTypeMapper.toRoomTypeResponse(savedRoomType);
    }

    public RoomTypeResponse findById(Long id) {
        return roomTypeMapper.toRoomTypeResponse(roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND)));
    }

    public List<RoomTypeResponse> findByBranchId(String branchId) {

        if (!branchRepository.existsById(branchId)) {
            throw new AppException(ErrorCode.BRANCH_NOT_FOUND);
        }

        return roomTypeRepository.findByBranchId(branchId)
                .stream()
                .map(roomTypeMapper::toRoomTypeResponse)
                .collect(Collectors.toList());
    }

    public List<RoomTypeSummaryResponse> getSummaryByBranch(String branchId) {

        List<RoomType> roomTypes = roomTypeRepository.findByBranchId(branchId);

        return roomTypes.stream().map(rt -> {

            // Ảnh chính
            String mainPhoto = roomPhotoRepository
                    .findByRoomTypeIdAndIsMainTrue(rt.getId())
                    .map(RoomPhoto::getPhotoUrl)
                    .orElse(null);

            // Số phòng
            Long roomCount = roomPhotoRepository.countByRoomTypeId(rt.getId());

            // Giá thấp nhất
            BigDecimal minPrice = priceRepository.findMinPriceByRoomTypeId(rt.getId());

            return RoomTypeSummaryResponse.builder()
                    .id(rt.getId())
                    .typeName(rt.getTypeName())
                    .capacity(rt.getCapacity())
                    .mainPhotoUrl(mainPhoto)
                    .roomCount(roomCount)
                    .minPrice(minPrice)
                    .build();

        }).toList();
    }
}
