package me.aikin.bicyclestore.user.api.auth;


import lombok.extern.slf4j.Slf4j;
import me.aikin.bicyclestore.user.api.auth.playload.JwtAuthenticationResponse;
import me.aikin.bicyclestore.user.api.auth.playload.LoginRequest;
import me.aikin.bicyclestore.user.api.auth.playload.SignUpRequest;
import me.aikin.bicyclestore.user.domain.User;
import me.aikin.bicyclestore.user.playload.ApiResponse;
import me.aikin.bicyclestore.user.repository.UserRepository;
import me.aikin.bicyclestore.user.security.jwt.AuthService;
import me.aikin.bicyclestore.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = authService.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Starting register user, username={}", signUpRequest.getUsername());

        if (userRepository.existsByUsername(signUpRequest.getUsername()) ||
            userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
            .name(signUpRequest.getName())
            .email(signUpRequest.getEmail())
            .username(signUpRequest.getUsername())
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .build();
        User savedUser = userService.createUser(user);

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/users/{username}")
            .buildAndExpand(savedUser.getUsername()).toUri();

        log.info("End register user, username={}", signUpRequest.getUsername());

        return ResponseEntity.created(location)
            .body(new ApiResponse(true, "User registered successfully"));
    }

}
