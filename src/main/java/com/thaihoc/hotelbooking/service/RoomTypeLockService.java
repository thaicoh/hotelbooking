package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.response.RoomTypeLockResponse;
import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.entity.RoomTypeLock;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.RoomTypeLockMapper;
import com.thaihoc.hotelbooking.repository.BookingTypeRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeLockRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomTypeLockService {
    @Autowired
    private RoomTypeLockRepository lockRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private BookingTypeRepository bookingTypeRepository;

    @Autowired
    private RoomTypeLockMapper roomTypeLockMapper;

    @Transactional
    public RoomTypeLock createLock(Long roomTypeId, Long bookingTypeId, User user,
                                   LocalDateTime lockedAt, LocalDateTime unlockedAt, String remarks) {
        if (unlockedAt.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "invalid unlocked time");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        BookingType bookingType = bookingTypeRepository.findById(bookingTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TYPE_NOT_FOUND));

        RoomTypeLock lock = RoomTypeLock.builder()
                .roomType(roomType)
                .bookingType(bookingType)
                .lockedBy(user.getEmail())
                .lockedAt(lockedAt != null ? lockedAt : LocalDateTime.now())
                .unlockedAt(unlockedAt)
                .remarks(remarks)
                .build();

        return lockRepository.save(lock);
    }

    @Transactional
    public String deleteLock(Long lockId) {
        if (!lockRepository.existsById(lockId)) {
            throw new AppException(ErrorCode.UNHANDLED_EXCEPTION, "lock not found");
        }
        lockRepository.deleteById(lockId);
        return "Lock deleted successfully";
    }

    public List<RoomTypeLockResponse> getLocksByBranch(String branchId) {
        return roomTypeLockMapper.toRoomTypeLockResponseList(lockRepository.findByRoomTypeBranchId(branchId));
    }


}
