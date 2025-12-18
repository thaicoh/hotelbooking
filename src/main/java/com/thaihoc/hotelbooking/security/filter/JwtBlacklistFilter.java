package com.thaihoc.hotelbooking.security.filter;

import com.thaihoc.hotelbooking.configuration.SecurityBeansConfig;
import com.thaihoc.hotelbooking.repository.InvalidatedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtDecoder jwtDecoder;

    public JwtBlacklistFilter(InvalidatedTokenRepository invalidatedTokenRepository,
                              JwtDecoder jwtDecoder) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Jwt jwt = jwtDecoder.decode(token);
                String jti = jwt.getId();

                if (invalidatedTokenRepository.existsById(jti)) {
                    throw new BadCredentialsException("Token has been invalidated");
                }

            } catch (JwtException e) {
                // ⭐ QUAN TRỌNG: ném lỗi để Spring Security xử lý
                throw new BadCredentialsException("Invalid or expired token");
            }

        }

        filterChain.doFilter(request, response);
    }
}
