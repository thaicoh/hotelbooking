package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingTypeRepository extends JpaRepository<BookingType, Long> {
    boolean existsByCode(String code);

    Optional<BookingType> findByCode(String code);
}
