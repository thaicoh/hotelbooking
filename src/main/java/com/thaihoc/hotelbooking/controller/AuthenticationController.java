package com.thaihoc.hotelbooking.controller;

import com.nimbusds.jose.JOSEException;
import com.thaihoc.hotelbooking.dto.request.AuthenticationRequest;
import com.thaihoc.hotelbooking.dto.request.IntrospectRequest;
import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import com.thaihoc.hotelbooking.dto.response.AuthenticationResponse;
import com.thaihoc.hotelbooking.dto.response.IntrospectResponse;
import com.thaihoc.hotelbooking.service.AuthenticationService;
import jakarta.validation.Valid;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping()
    ApiResponse<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(
                        AuthenticationResponse.builder()
                                .token(authenticationService.authenticate(request))
                                .authenticated(true)
                                .build()
                )
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }



}
