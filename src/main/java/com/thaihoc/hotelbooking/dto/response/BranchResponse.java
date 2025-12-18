package com.thaihoc.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchResponse {

    private String id;

    private String branchName;

    private String address;

    private String phone;

    private String email;

    private String photoUrl;

    private LocalDateTime createdAt;
}
