package com.example.shinhangaecheokja.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "marketing_agreed", nullable = false)
    private boolean marketingAgreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "courier_vehicle", length = 50)
    private String courierVehicle;

    @Column(name = "activity_region", length = 100)
    private String activityRegion;

    @Column(name = "preferred_weight", length = 30)
    private String preferredWeight;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private User(String name, String email, String password, String phoneNumber, boolean marketingAgreed, Role role, String courierVehicle, String activityRegion, String preferredWeight) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.marketingAgreed = marketingAgreed;
        this.role = role;
        this.courierVehicle = courierVehicle;
        this.activityRegion = activityRegion;
        this.preferredWeight = preferredWeight;
    }

    public static User create(String name, String email, String encodedPassword, String phoneNumber, boolean marketingAgreed) {
        return new User(name, email, encodedPassword, phoneNumber, marketingAgreed, Role.USER, null, null, null);
    }

    public static User createAdmin(String name, String email, String encodedPassword, String phoneNumber, boolean marketingAgreed) {
        return new User(name, email, encodedPassword, phoneNumber, marketingAgreed, Role.ADMIN, null, null, null);
    }

    public static User createCourier(String name, String email, String encodedPassword, String phoneNumber, boolean marketingAgreed, String courierVehicle, String activityRegion, String preferredWeight) {
        return new User(name, email, encodedPassword, phoneNumber, marketingAgreed, Role.COURIER, courierVehicle, activityRegion, preferredWeight);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
