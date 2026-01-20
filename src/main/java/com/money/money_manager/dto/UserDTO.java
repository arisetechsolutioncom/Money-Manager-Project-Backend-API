package com.money.money_manager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String profileImage;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Stats
    private Long totalTransactions;
    private Long activeBudgets;
    private Long financialGoals;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateUserRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank
    private String firstName;

    private String lastName;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}
