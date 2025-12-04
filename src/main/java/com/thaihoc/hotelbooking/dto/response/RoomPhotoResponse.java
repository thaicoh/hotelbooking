package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.entity.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPhotoResponse {

    private Long id;

    private String photoUrl;

    private Boolean isMain;

    private RoomTypeResponse roomType;
}
