package com.money.money_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private RoleName name;

    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RoleName {
        ADMIN,
        USER,
        PREMIUM_USER
    }
}
