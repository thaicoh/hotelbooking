package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByRoles(Role roles);

    Optional<User> findByEmail(String s);
    Optional<User> findByPhone(String s);

    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN u.roles r
    WHERE 
        (:search IS NULL 
         OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))
      AND (:status IS NULL OR u.status = :status)
    """)
    Page<User> searchAndFilter(String search, String role, UserStatus status, Pageable pageable);

}
