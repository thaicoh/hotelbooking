package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Branch;
import com.thaihoc.hotelbooking.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BranchRepository extends JpaRepository<Branch,String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByBranchName(String branchName);

    @Query("""
        SELECT b FROM Branch b
        WHERE LOWER(b.branchName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Branch> searchByNameOrAddress(@Param("search") String search, Pageable pageable);

}
