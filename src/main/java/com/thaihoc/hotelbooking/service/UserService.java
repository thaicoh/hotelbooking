package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.AdminCreateUserRequest;
import com.thaihoc.hotelbooking.dto.request.UserCreationRequest;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.dto.response.UserResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.UserStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.UserMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    BranchRepository branchRepository;

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

        user.setCreatedAt(LocalDateTime.now());

        user.setStatus(UserStatus.ACTIVE); // trạng thái mặc định cho role customer

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<UserResponse> getAll(){
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public boolean delete(String userId){
        // check
        if(!userRepository.existsById(userId)){
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }
    public PageResponse<UserResponse> getAllPaging(int page, int size, String search, String role,String status) {

        Pageable pageable = PageRequest.of(page, size);

        UserStatus statusEnum = null;

        if (status != null && !status.isBlank()) {
            statusEnum = UserStatus.valueOf(status);
        }

        Page<User> users = userRepository.searchAndFilter(search, role, statusEnum, pageable);

        return PageResponse.<UserResponse>builder()
                .items(users.getContent().stream().map(userMapper::toUserResponse).toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public UserResponse createByAdmin(AdminCreateUserRequest request){

        // 1. Check email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        // 2. MapStruct map các field cơ bản
        User user = userMapper.toUser(request);

        // ⭐ 2.1. Set status (nếu không gửi thì ACTIVE)
        if (request.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(request.getStatus());
        }

        // 3. Encode password
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 4. Gán roles theo request
        Set<Role> roles = roleRepository.findByNameIn(request.getRoles());
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        user.setRoles(roles);

        // 5. Nếu là STAFF → bắt buộc có branchId
        if (request.getRoles().contains("STAFF")) {
            if (request.getBranchId() == null) {
                throw new AppException(ErrorCode.USER_CREATION_FAILED);
            }

            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }

        // 6. Set thời gian tạo
        user.setCreatedAt(LocalDateTime.now());

        // 7. Lưu user
        user = userRepository.save(user);

        // 8. Trả về response
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public UserResponse updateStatus(String email, UserStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(status);
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_STAFF')")
    public UserResponse getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }



}
