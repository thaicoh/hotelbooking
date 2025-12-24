package com.thaihoc.hotelbooking.service;

import com.thaihoc.hotelbooking.dto.request.BranchCreationRequest;
import com.thaihoc.hotelbooking.dto.request.BranchUpdateRequest;
import com.thaihoc.hotelbooking.dto.response.BranchResponse;
import com.thaihoc.hotelbooking.dto.response.PageResponse;
import com.thaihoc.hotelbooking.dto.response.RoomTypeSummaryResponse;
import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.RoomPhoto;
import com.thaihoc.hotelbooking.entity.RoomType;
import com.thaihoc.hotelbooking.enums.BranchStatus;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.BranchMapper;
import com.thaihoc.hotelbooking.repository.BranchRepository;
import com.thaihoc.hotelbooking.repository.RoomPhotoRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeBookingTypePriceRepository;
import com.thaihoc.hotelbooking.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchMapper branchMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomPhotoRepository roomPhotoRepository;




    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public BranchResponse create(BranchCreationRequest request, MultipartFile photo) {

        if (branchRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.BRANCH_EMAIL_ALREADY_EXISTS);
        }

        if (branchRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.BRANCH_PHONE_ALREADY_EXISTS);
        }

        if (branchRepository.existsByBranchName(request.getBranchName())) {
            throw new AppException(ErrorCode.BRANCH_NAME_ALREADY_EXISTS);
        }

        Branch branch = branchMapper.toBranch(request);

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo, "branches/");
            branch.setPhotoUrl(photoUrl);
        }

        Branch saved = branchRepository.save(branch);
        return branchMapper.toBranchResponse(saved);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
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

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public BranchResponse update(String id, BranchUpdateRequest request, MultipartFile photo) {
        Branch  branch = branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND));
        branchMapper.updateBranch(branch, request);

        if (photo != null && !photo.isEmpty()) {
            String newPhotoUrl =  fileStorageService.update(photo, branch.getPhotoUrl(), "branches/");
            branch.setPhotoUrl(newPhotoUrl);
        }

        Branch saved = branchRepository.save(branch);

        return branchMapper.toBranchResponse(saved);
    }

    public BranchResponse getById(String id) {
        return branchMapper.toBranchResponse(branchRepository.findById(id).orElseThrow(() ->  new AppException(ErrorCode.BRANCH_NOT_FOUND)));
    }

    public PageResponse<BranchResponse> getAllPaging(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Branch> branchPage;

        if (search != null && !search.isBlank()) {
            branchPage = branchRepository.searchByNameOrAddress(search, pageable);
        } else {
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchResponse> responses = branchPage.getContent()
                .stream()
                .map(branchMapper::toBranchResponse)
                .toList();

        return PageResponse.<BranchResponse>builder()
                .page(page)
                .size(size)
                .totalElements(branchPage.getTotalElements())
                .totalPages(branchPage.getTotalPages())
                .items(responses)
                .build();
    }

    public BranchResponse updateStatus(String id, BranchStatus status) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        branch.setStatus(status);
        Branch saved = branchRepository.save(branch);

        return branchMapper.toBranchResponse(saved);
    }


}
