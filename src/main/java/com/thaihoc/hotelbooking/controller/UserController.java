package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping()
    UserResponse create(@Valid @RequestBody UserCreationRequest request){
        return userService.create(request);
    }

    @GetMapping()
    ApiResponse<List<UserResponse>> getAll(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> delete( @PathVariable("userId") String userId){
        return ApiResponse.<String>builder()
                .result(userService.delete(userId)?"User has been deleted":"User not found")
                .build();
    }

}
