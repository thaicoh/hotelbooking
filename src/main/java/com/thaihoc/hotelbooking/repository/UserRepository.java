package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByRoles(Role roles);

    Optional<User> findByEmail(String s);
    Optional<User> findByPhone(String s);
}
