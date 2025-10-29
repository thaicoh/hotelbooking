package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.PermissionCreationRequest;
import com.thaihoc.hotelbooking.dto.response.PermissionResponse;
import com.thaihoc.hotelbooking.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreationRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
