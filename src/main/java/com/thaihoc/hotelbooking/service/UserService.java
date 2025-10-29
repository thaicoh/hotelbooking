package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.UserMapper;
import com.thaihoc.hotelbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;




import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public UserResponse create(UserCreationRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

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
