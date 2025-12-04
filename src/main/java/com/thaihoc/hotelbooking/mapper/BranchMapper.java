package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    Branch toBranch(BranchCreationRequest request);
    BranchResponse toBranchResponse(Branch branch);

    void updateBranch(@MappingTarget Branch branch, BranchUpdateRequest request);
}
