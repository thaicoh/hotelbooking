package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchMapper branchMapper;

    @Autowired
    private FileStorageService fileStorageService;


    public BranchResponse create(BranchCreationRequest request, MultipartFile photo) {
        if (branchRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        if (branchRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("PHONE_ALREADY_EXISTS");
        }

        Branch branch = branchMapper.toBranch(request);

        if (!photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo, "branches/");
            branch.setPhotoUrl(photoUrl);
        }

        Branch saved = branchRepository.save(branch);
        return branchMapper.toBranchResponse(saved);
    }

    public String delete(String id) {
        if (branchRepository.existsById(id)) {
            branchRepository.deleteById(id);
            return "Branch has been deleted";
        }
        return "Branch not found";
    }

    public List<Branch> getAll() {
        return branchRepository.findAll();
    }

    public BranchResponse update(String id, BranchUpdateRequest request, MultipartFile photo) {
        Branch  branch = branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND));
        branchMapper.updateBranch(branch, request);

        if (!photo.isEmpty()) {
            String newPhotoUrl =  fileStorageService.update(photo, branch.getPhotoUrl(), "branches/");
            branch.setPhotoUrl(newPhotoUrl);
        }

        Branch saved = branchRepository.save(branch);

        return branchMapper.toBranchResponse(saved);
    }

    public BranchResponse getById(String id) {
        return branchMapper.toBranchResponse(branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND)));
    }

}
