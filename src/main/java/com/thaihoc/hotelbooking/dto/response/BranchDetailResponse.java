package com.thaihoc.hotelbooking.dto.response;

import com.thaihoc.hotelbooking.enums.BranchStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BranchDetailResponse {
    private String branchId;
    private String branchName;
    private BranchStatus branchStatus;
    private String address;
    private String photoUrl;
    private List<RoomTypeDetailResponse> rooms;
}

