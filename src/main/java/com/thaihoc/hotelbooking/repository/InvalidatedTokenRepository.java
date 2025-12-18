package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    boolean existsById(String id);
}
