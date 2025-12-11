package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.UserMapper;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleRepository roleRepository;

    private final OtpStore otpStore;

    public UserResponse create(UserCreationRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        // ✅ 1. Email phải được verify OTP trước
        if (!otpStore.isVerified(request.getEmail())) {
            throw new AppException(ErrorCode.USER_CREATION_FAILED);
        }






        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById("CUSTOMER").ifPresent(role -> roles.add(role));

        user.setRoles(roles);

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<UserResponse> getAll(){
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public boolean delete(String userId){
        // check
        if(!userRepository.existsById(userId)){
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }
}
