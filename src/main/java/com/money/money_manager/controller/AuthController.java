package com.money.money_manager.controller;

import com.money.money_manager.config.JwtTokenProvider;
import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.UserDTO;
import com.money.money_manager.entity.User;
import com.money.money_manager.entity.Role;
import com.money.money_manager.repository.RoleRepository;
import com.money.money_manager.repository.UserRepository;
import com.money.money_manager.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Email already in use", null));
        }

        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(Role.RoleName.USER);
                    role.setDescription("Default User Role");
                    return roleRepository.save(role);
                });

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(userRole)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User registered successfully", userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        log.info("Login request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid email or password", null));
        }

        if (!user.getIsActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "User account is inactive", null));
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        // Set httpOnly cookie using proper Cookie object
        Cookie authCookie = new Cookie("authToken", token);
        authCookie.setPath("/");
        authCookie.setMaxAge((int) (jwtExpiration / 1000));
        authCookie.setHttpOnly(true);
        authCookie.setSecure(false); // false for localhost/development, true for production HTTPS
        authCookie.setAttribute("SameSite", "Strict");
        response.addCookie(authCookie);

        LoginResponse responseData = new LoginResponse(null, userDTO); // Don't send token in response
        log.info("User logged in successfully: {}", user.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", responseData));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isValid = jwtTokenProvider.validateToken(token);

        if (isValid) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            UserDTO userDTO = userService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Token is valid", userDTO));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Invalid token", null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token", null));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null));
        }

        String newToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed", newToken));
    }
}

@Data
@NoArgsConstructor
class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters long")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
}

@Data
@NoArgsConstructor
class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}

@Data
@NoArgsConstructor
class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}
