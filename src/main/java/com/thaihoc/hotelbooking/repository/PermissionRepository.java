package com.thaihoc.hotelbooking.repository;

import com.thaihoc.hotelbooking.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
