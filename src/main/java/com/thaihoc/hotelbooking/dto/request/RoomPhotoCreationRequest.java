package com.thaihoc.hotelbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomPhotoCreationRequest {
    private Long roomTypeId;

    private List<MultipartFile> photos;

    private Integer mainPhotoIndex;
}
