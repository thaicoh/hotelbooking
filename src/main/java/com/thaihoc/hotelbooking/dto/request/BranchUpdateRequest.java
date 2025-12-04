package com.thaihoc.hotelbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchUpdateRequest {
    private String branchName;

    private String address;

    private String phone;

    private String email;
}
