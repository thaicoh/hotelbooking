package com.thaihoc.hotelbooking.service;

import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.thaihoc.hotelbooking.dto.request.AuthenticationRequest;
import com.thaihoc.hotelbooking.dto.request.IntrospectRequest;
import com.thaihoc.hotelbooking.dto.response.AuthenticationResponse;
import com.thaihoc.hotelbooking.dto.response.IntrospectResponse;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.exception.AppException;
import com.thaihoc.hotelbooking.exception.ErrorCode;
import com.thaihoc.hotelbooking.mapper.UserMapper;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.*;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;


@Service
public class AuthenticationService {
    @Autowired
    UserRepository userRepository;

    @NonFinal
    protected final String SINGER_KEY = "/q/Ur0Q2M4h8Csuu67iSpYIg3JlpLD7Ex6nZZMvbt9QvcK0RDwpKtAIIg86jRKSG";

    @Autowired
    UserMapper userMapper;

    public AuthenticationResponse authenticate(AuthenticationRequest request){

        User user = new User();

        AuthenticationResponse response = new AuthenticationResponse();

        if(userRepository.existsByPhone(request.getPhoneOrEmail())){
            user = userRepository.findByPhone(request.getPhoneOrEmail()).orElseThrow(() -> new AppException(ErrorCode.UNHANDLED_EXCEPTION));
        } else if (userRepository.existsByEmail(request.getPhoneOrEmail())) {
            user = userRepository.findByEmail(request.getPhoneOrEmail()).orElseThrow(() -> new AppException(ErrorCode.UNHANDLED_EXCEPTION));
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }


        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(genToken(user))
                .user(userMapper.toUserResponse(user))
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();

        boolean valid = true;


        try {
            verifyToken(token);

        }catch (AppException exception){
            valid = false;
        }

        return IntrospectResponse.builder()
                .valid(valid)
                .build();


    }

    private String genToken(User user)  {
        JWSHeader jwsHeader = new  JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("hotel_booking.con")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
            return jwsObject.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());

                if(!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }

        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier jwsVerifier = new MACVerifier(SINGER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(jwsVerifier);

        if(!verified) {
            System.out.println("!verified");
            throw new AppException(ErrorCode.UNAUTHENTICATED);}


        if (new Date().after(exp)) {
            // Token đã hết hạn
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }


        return signedJWT;
    }
}
