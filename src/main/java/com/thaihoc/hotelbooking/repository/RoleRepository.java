package com.thaihoc.hotelbooking.repository;


import com.thaihoc.hotelbooking.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, String> {
    // Spring Data JPA sẽ tự tạo query dựa trên tên hàm
    Set<Role> findByNameIn(Set<String> names);

}
