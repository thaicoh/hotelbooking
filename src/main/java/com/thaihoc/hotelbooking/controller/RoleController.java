package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.request.PermissionCreationRequest;
import com.thaihoc.hotelbooking.dto.request.RoleCreationRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.PermissionResponse;
import com.thaihoc.hotelbooking.dto.response.RoleResponse;
import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.service.PermissionService;
import com.thaihoc.hotelbooking.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;

    @PostMapping()
    private ApiResponse<RoleResponse> create(@RequestBody RoleCreationRequest request){
        return  ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping()
    private ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{id}")
    private ApiResponse<Void> delete(@PathVariable String id){
        roleService.delete(id);
        return ApiResponse.<Void>builder()
                .build();

    }
}
