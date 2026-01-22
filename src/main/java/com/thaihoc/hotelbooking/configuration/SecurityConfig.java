package com.thaihoc.hotelbooking.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.security.filter.JwtBlacklistFilter;
import com.thaihoc.hotelbooking.security.filter.UserStatusFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.signerKey}")
    private String jwtSignerKey;

    @Autowired
    private SecurityBeansConfig securityBeansConfig;

    @Autowired
    private JwtBlacklistFilter jwtBlacklistFilter;

    @Autowired
    private UserStatusFilter userStatusFilter;

    private final String [] PUBLIC_ENDPOINTS = {"/users","/auth","/auth/**","/branch/search-hotels", "/branch/*/hotel-detail", "/reviews/branch/*/paged"};

    private final String [] PUBLIC_TEST_ENDPOINTS = {"/room_type" ,

            "/room_type/**", "/room_photo", "/room_photo/**", "/room", "/room/**", "/room/**",

            "/booking_type", "/booking_type/**", "/room-type-booking-type-prices", "/room-type-booking-type-prices/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(requests ->
                requests.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/branches/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/rooms/**").permitAll()
                        .requestMatchers("/api/vnpay/**").permitAll()
                        .requestMatchers(
                                "/ws-bookings/**",
                                "/ws-bookings/info/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, PUBLIC_TEST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_TEST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.DELETE, PUBLIC_TEST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.PUT, PUBLIC_TEST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.PATCH, PUBLIC_TEST_ENDPOINTS).permitAll()

                        .anyRequest().authenticated());


        // ⭐ Filter kiểm tra token bị revoke
        httpSecurity.addFilterBefore(
                jwtBlacklistFilter,
                BearerTokenAuthenticationFilter.class
        );
        // ⭐ Filter kiểm tra trạng thái user (LOGIN_LOCKED)
        httpSecurity.addFilterAfter(
                userStatusFilter,
                BearerTokenAuthenticationFilter.class
        );


        // ⭐ BẮT BUỘC: Tắt session → JWT được kiểm tra mỗi request
        httpSecurity.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // ⭐ Cấu hình JWT + EntryPoint
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(securityBeansConfig.jwtDecoder()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        // ⭐ Gắn AccessDeniedHandler vào exceptionHandling
        httpSecurity.exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
        );


        // ⭐ Bật CORS
        httpSecurity.cors(Customizer.withDefaults());

        // ⭐ Tắt CS RF
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern("*");   // ✅ Cho phép tất cả origin
        config.addAllowedHeader("*");          // ✅ Cho phép tất cả header
        config.addAllowedMethod("*");          // ✅ Cho phép tất cả method: GET, POST, PUT, DELETE...
        config.setAllowCredentials(true);      // ✅ Cho phép gửi cookie / token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .code(ErrorCode.UNAUTHORIZE.getCode())
                    .message("Access Denied")
                    .build();

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
        };
    }


}