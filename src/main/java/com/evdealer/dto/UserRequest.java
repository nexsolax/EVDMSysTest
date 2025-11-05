package com.evdealer.dto;

import com.evdealer.enums.UserType;
import com.evdealer.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "User request DTO for user management")
public class UserRequest {
    
    @Schema(description = "First name", example = "John", required = true)
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe", required = true)
    private String lastName;
    
    @Schema(description = "Username", example = "johndoe")
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "Phone number", example = "0123456789")
    private String phone;
    
    @Schema(description = "Password", example = "password123", required = true)
    private String password;
    
    @Schema(description = "User type", example = "DEALER_STAFF", allowableValues = {"ADMIN", "EVM_STAFF", "DEALER_MANAGER", "DEALER_STAFF"})
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
    public UserRequest() {}
    
    public UserRequest(String firstName, String lastName, String email, String password, UserType userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }
    
    public UserRequest(String firstName, String lastName, String username, String email, String password, UserType userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }
    
    // Getters and Setters
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
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
