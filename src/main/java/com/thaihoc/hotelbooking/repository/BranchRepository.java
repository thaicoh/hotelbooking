package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch,String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
