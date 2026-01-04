package com.thaihoc.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BranchDetailResponse {
    private String branchId;
    private String branchName;
    private String address;
    private String photoUrl;
    private List<RoomTypeDetailResponse> rooms;
}

