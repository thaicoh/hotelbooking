package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
