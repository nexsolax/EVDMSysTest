package com.evdealer.dto;

import com.evdealer.enums.UserType;
import com.evdealer.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "User update request DTO for updating user information")
public class UserUpdateRequest {
    
    @Schema(description = "Username", example = "admin")
    private String username;
    
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Phone number", example = "0123456789")
    private String phone;
    
    @Schema(description = "Address", example = "123 Main Street")
    private String address;
    
    @Schema(description = "Date of birth", example = "1990-01-01")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Profile image URL", example = "https://example.com/image.jpg")
    private String profileImageUrl;
    
    @Schema(description = "Profile image path", example = "/uploads/profile.jpg")
    private String profileImagePath;
    
    @Schema(description = "User type", example = "ADMIN", allowableValues = {"ADMIN", "EVM_STAFF", "DEALER_MANAGER", "DEALER_STAFF"})
    private UserType userType;
    
    @Schema(description = "User status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
    private UserStatus status;
    
    @Schema(description = "Dealer ID", example = "78fe7eb0-ceb8-4793-a8af-187a3fe26f67")
    private UUID dealerId;
    
    @Schema(description = "Dealer Name (alternative to dealerId)", example = "EV Đại lý 1 test")
    private String dealerName;
    
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;
    
    // Constructors
    public UserUpdateRequest() {}
    
    public UserUpdateRequest(String username, String firstName, String lastName, String email, String phone, UserType userType, UserStatus status, Boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.status = status;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public String getProfileImagePath() {
        return profileImagePath;
    }
    
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public UUID getDealerId() {
        return dealerId;
    }
    
    public void setDealerId(UUID dealerId) {
        this.dealerId = dealerId;
    }
    
    public String getDealerName() {
        return dealerName;
    }
    
    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

