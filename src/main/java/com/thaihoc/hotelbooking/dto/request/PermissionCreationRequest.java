package com.thaihoc.hotelbooking.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionCreationRequest {
    private String name;
    private String description;
}
