package com.bookingSystem.contorller;

import com.bookingSystem.dto.LoginRequest;
import com.bookingSystem.dto.RegisterRequest;
import com.bookingSystem.dto.UserResponse;
import com.bookingSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        if (role != null) {
            Instant now = Instant.now();
            // PAYLOAD
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .subject(username)
                    .claim("role", role.replace("ROLE_",""))
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();

            // HEADER
            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

            // SIGNATURE
            String token = jwtEncoder
                    .encode(JwtEncoderParameters.from(header, claims))
                    .getTokenValue();
            return ResponseEntity.ok(token);
        }
        throw new RuntimeException("Role is null!");
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody RegisterRequest request){
        UserResponse response = this.userService.addUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}