package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.AdminCreateUserRequest;
import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "branchId", source = "branch.id")
    UserResponse toUserResponse(User  user);

    // ⭐ Thêm method mới để map từ AdminCreateUserRequest → User
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "branch", ignore = true)
    User toUser(AdminCreateUserRequest request);
}
