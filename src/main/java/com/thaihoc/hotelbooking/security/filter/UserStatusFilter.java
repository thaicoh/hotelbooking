package com.thaihoc.hotelbooking.security.filter;

import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.enums.UserStatus;
import com.thaihoc.hotelbooking.repository.UserRepository;
import com.thaihoc.hotelbooking.security.entryPoint.LoginLockedEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserStatusFilter extends OncePerRequestFilter {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginLockedEntryPoint loginLockedEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {

            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && user.getStatus() == UserStatus.LOGIN_LOCKED) {
                loginLockedEntryPoint.commence(request, response, null);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }


}
