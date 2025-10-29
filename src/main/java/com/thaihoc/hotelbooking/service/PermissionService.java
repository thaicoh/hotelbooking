package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.PermissionCreationRequest;
import com.thaihoc.hotelbooking.dto.response.PermissionResponse;
import com.thaihoc.hotelbooking.entity.Permission;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.PermissionMapper;
import com.thaihoc.hotelbooking.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionCreationRequest request){

        if(permissionRepository.existsById(request.getName())){
            throw new AppException(ErrorCode.PERMISSION_ALREADY_EXISTS);
        }

        Permission permission = permissionMapper.toPermission(request);
        Permission saved  = permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(saved);
    }

    public List<PermissionResponse> getAll(){
        return permissionRepository.findAll().stream().map(permission -> permissionMapper.toPermissionResponse(permission)).toList();
    }
}
