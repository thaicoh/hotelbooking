package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.RoleCreationRequest;
import com.thaihoc.hotelbooking.dto.response.RoleResponse;
import com.thaihoc.hotelbooking.entity.Permission;
import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.RoleMapper;
import com.thaihoc.hotelbooking.repository.PermissionRepository;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    UserRepository userRepository;

    public RoleResponse create(RoleCreationRequest request){

        if(roleRepository.existsById(request.getName()))
            throw new AppException(ErrorCode.ROLE_NAME_ALREADY_EXISTS);


        Role role = roleMapper.toRole(request);

        Set<Permission> permissions = request.getPermissions().stream().map(
                s -> permissionRepository.findById(s).orElseThrow( () -> new AppException(ErrorCode.PERMISSION_NOT_FOUND))
        ).collect(Collectors.toSet());

        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);

        return roleMapper.toRoleResponse(saved);
    }

    public List<RoleResponse> getAll(){
        return roleRepository.findAll().stream().map(role -> roleMapper.toRoleResponse(role)).toList();
    }

    public void delete(String id){
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if(userRepository.existsByRoles(role)){
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        roleRepository.deleteById(id);
    }
}   
