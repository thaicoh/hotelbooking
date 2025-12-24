package com.thaihoc.hotelbooking.configuration;

import com.thaihoc.hotelbooking.entity.BookingType;
import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.UserStatus;
import com.thaihoc.hotelbooking.repository.BookingTypeRepository;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            BookingTypeRepository bookingTypeRepository
    ) {
        return args -> {
            // 1. Tạo role mặc định
            createRoleIfNotExists(roleRepository, "ADMIN", "Quyền quản trị hệ thống");
            createRoleIfNotExists(roleRepository, "STAFF", "Quyền nhân viên");
            createRoleIfNotExists(roleRepository, "CUSTOMER", "Quyền khách hàng");

            // 2. Tạo user admin mặc định
            if (userRepository.findByEmail("admin").isEmpty()) {
                Role adminRole = roleRepository.findById("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

                HashSet<Role> roles = new HashSet<>();
                roles.add(adminRole);

                User adminUser = User.builder()
                        .fullName("admin")
                        .email("admin")
                        .roles(roles)
                        .status(UserStatus.ACTIVE)
                        .passwordHash(passwordEncoder.encode("admin"))
                        .phone("00000000")
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(adminUser);
                log.info("Admin user has been created with default password, please change it");
            }

            // 3. Tạo BookingType mặc định nếu chưa có
            createBookingTypeIfNotExists(
                    bookingTypeRepository,
                    "DAY",
                    "Ngày",
                    "Đặt phòng theo ngày",
                    "Check-in 14h hôm nay, check-out 12h hôm sau",
                    LocalTime.of(14, 0),
                    LocalTime.of(12, 0),
                    22 // số giờ từ 14h -> 12h hôm sau
            );

            createBookingTypeIfNotExists(
                    bookingTypeRepository,
                    "HOUR",
                    "Giờ",
                    "Đặt phòng theo giờ",
                    "Chỉ đặt phòng trong khoảng từ 13h đến 20h",
                    LocalTime.of(13, 0),
                    LocalTime.of(20, 0),
                    null // durationHours có thể để null, vì tính theo số giờ khách chọn
            );


            createBookingTypeIfNotExists(
                    bookingTypeRepository,
                    "NIGHT",
                    "Đêm",
                    "Đặt phòng qua đêm",
                    "Check-in từ 21h, check-out 12h hôm sau",
                    LocalTime.of(21, 0),
                    LocalTime.of(12, 0),
                    15 // số giờ từ 21h -> 12h hôm sau
            );

        };
    }


    private void createRoleIfNotExists(RoleRepository roleRepository, String name, String description) {
        if (roleRepository.findById(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            log.info("Role {} has been created", name);
        }
    }

    private void createBookingTypeIfNotExists(
            BookingTypeRepository repo,
            String code,
            String name,
            String description,
            String rules,
            LocalTime checkIn,
            LocalTime checkOut,
            Integer durationHours
    ) {
        if (repo.findByCode(code).isEmpty()) {
            BookingType bt = BookingType.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .rules(rules)
                    .defaultCheckInTime(checkIn)
                    .defaultCheckOutTime(checkOut)
                    .durationHours(durationHours)
                    .build();
            repo.save(bt);
            log.info("BookingType {} has been created", code);
        }
    }



}
