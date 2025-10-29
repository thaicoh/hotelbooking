package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.RoleCreationRequest;
import com.thaihoc.hotelbooking.dto.response.RoleResponse;
import com.thaihoc.hotelbooking.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);


}
