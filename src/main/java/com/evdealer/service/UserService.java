package com.evdealer.service;

import com.evdealer.dto.UserRequest;
import com.evdealer.dto.UserUpdateRequest;
import com.evdealer.entity.Dealer;
import com.evdealer.entity.User;
import com.evdealer.repository.DealerRepository;
import com.evdealer.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, 
                      DealerRepository dealerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dealerRepository = dealerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // Use findAllWithDetails to eagerly load dealer relationship
        return userRepository.findAllWithDetails();
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID userId) {
        // Use findByIdWithDealer to eagerly load dealer relationship
        Optional<User> userWithDealer = userRepository.findByIdWithDealer(userId);
        if (userWithDealer.isPresent()) {
            return userWithDealer;
        }
        // Fallback to regular findById
        return userRepository.findById(userId);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        // Use findByUsernameWithDealer to eagerly load dealer relationship
        Optional<User> userWithDealer = userRepository.findByUsernameWithDealer(username);
        if (userWithDealer.isPresent()) {
            return userWithDealer;
        }
        // Fallback to regular findByUsername
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getUsersByRole(String roleName) {
        try {
            com.evdealer.enums.UserType userType = com.evdealer.enums.UserType.valueOf(roleName.toUpperCase());
            return userRepository.findByRoleName(userType);
        } catch (IllegalArgumentException e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public List<User> getUsersByDealer(UUID dealerId) {
        return userRepository.findByDealerDealerId(dealerId);
    }
    
    public List<User> getUsersByRoleString(String roleString) {
        try {
            com.evdealer.enums.UserType userType = com.evdealer.enums.UserType.valueOf(roleString.toUpperCase());
            return userRepository.findByRoleString(userType);
        } catch (IllegalArgumentException e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Validate dealer requirement: Non-admin users must have dealer
        if (user.getUserType() != null && user.getUserType() != com.evdealer.enums.UserType.ADMIN) {
            if (user.getDealer() == null) {
                throw new RuntimeException("Dealer is required for non-admin users. Please set dealer before creating user");
            }
        }
        
        // Hash password before saving
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            // Only hash if it's not already hashed (doesn't start with BCrypt prefix)
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        return userRepository.save(user);
    }
    
    public User createUserFromRequest(UserRequest request) {
        // Use provided username or generate from email/name if not provided
        String username;
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Use provided username
            username = request.getUsername().trim();
        } else {
            // Generate username from email if not provided
            username = request.getEmail() != null ? 
                request.getEmail().split("@")[0] : 
                (request.getFirstName() + "." + request.getLastName()).toLowerCase();
        }
        
        // Check for duplicate username
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis();
        }
        
        // Check for duplicate email
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        // Note: User entity doesn't have notes field, so we skip it
        
        // Validate dealer requirement: Non-admin users must have dealer
        com.evdealer.enums.UserType userType = request.getUserType() != null ? request.getUserType() : com.evdealer.enums.UserType.DEALER_STAFF;
        if (userType != com.evdealer.enums.UserType.ADMIN) {
            // Non-admin users must have dealer
            Dealer dealer = null;
            
            // Try to get dealer by ID first
            if (request.getDealerId() != null) {
                dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + request.getDealerId()));
            } 
            // Try to get dealer by name if ID not provided
            else if (request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
                dealer = dealerRepository.findByDealerName(request.getDealerName().trim())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + request.getDealerName()));
            }
            
            if (dealer == null) {
                throw new RuntimeException("Dealer is required for non-admin users. Please provide dealerId or dealerName");
            }
            
            user.setDealer(dealer);
        } else {
            // Admin users can optionally have dealer, but it's not required
            if (request.getDealerId() != null) {
                Dealer dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + request.getDealerId()));
                user.setDealer(dealer);
            } else if (request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
                Dealer dealer = dealerRepository.findByDealerName(request.getDealerName().trim())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + request.getDealerName()));
                user.setDealer(dealer);
            }
        }
        
        // Set user type and status
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(UUID userId, UserUpdateRequest userUpdateRequest) {
        // Use findByIdWithDealer to eagerly load dealer relationship
        User user = userRepository.findByIdWithDealer(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Log incoming data for debugging
        System.out.println("=== USER SERVICE UPDATE DEBUG ===");
        System.out.println("Existing user username: " + user.getUsername());
        System.out.println("New username: " + userUpdateRequest.getUsername());
        System.out.println("New email: " + userUpdateRequest.getEmail());
        System.out.println("New user type: " + userUpdateRequest.getUserType());
        System.out.println("New status: " + userUpdateRequest.getStatus());
        System.out.println("New is active: " + userUpdateRequest.getIsActive());
        System.out.println("==================================");
        
        // Check for duplicate username (excluding current user)
        if (userUpdateRequest.getUsername() != null && 
            !user.getUsername().equals(userUpdateRequest.getUsername()) && 
            userRepository.existsByUsername(userUpdateRequest.getUsername())) {
            throw new RuntimeException("Username already exists: " + userUpdateRequest.getUsername());
        }
        
        // Check for duplicate email (excluding current user)
        if (userUpdateRequest.getEmail() != null && 
            !user.getEmail().equals(userUpdateRequest.getEmail()) && 
            userRepository.existsByEmail(userUpdateRequest.getEmail())) {
            throw new RuntimeException("Email already exists: " + userUpdateRequest.getEmail());
        }
        
        // Update fields only if they are not null
        if (userUpdateRequest.getUsername() != null) {
            user.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getEmail() != null) {
            user.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getFirstName() != null) {
            user.setFirstName(userUpdateRequest.getFirstName());
        }
        if (userUpdateRequest.getLastName() != null) {
            user.setLastName(userUpdateRequest.getLastName());
        }
        if (userUpdateRequest.getPhone() != null) {
            user.setPhone(userUpdateRequest.getPhone());
        }
        if (userUpdateRequest.getAddress() != null) {
            user.setAddress(userUpdateRequest.getAddress());
        }
        if (userUpdateRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(userUpdateRequest.getDateOfBirth());
        }
        if (userUpdateRequest.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userUpdateRequest.getProfileImageUrl());
        }
        if (userUpdateRequest.getProfileImagePath() != null) {
            user.setProfileImagePath(userUpdateRequest.getProfileImagePath());
        }
        if (userUpdateRequest.getUserType() != null) {
            user.setUserType(userUpdateRequest.getUserType());
        }
        if (userUpdateRequest.getStatus() != null) {
            user.setStatus(userUpdateRequest.getStatus());
        }
        // Update dealer if provided
        if (userUpdateRequest.getDealerId() != null || (userUpdateRequest.getDealerName() != null && !userUpdateRequest.getDealerName().trim().isEmpty())) {
            Dealer dealer = null;
            
            // Try to get dealer by ID first
            if (userUpdateRequest.getDealerId() != null) {
                dealer = dealerRepository.findById(userUpdateRequest.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + userUpdateRequest.getDealerId()));
            } 
            // Try to get dealer by name if ID not provided
            else if (userUpdateRequest.getDealerName() != null && !userUpdateRequest.getDealerName().trim().isEmpty()) {
                dealer = dealerRepository.findByDealerName(userUpdateRequest.getDealerName().trim())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + userUpdateRequest.getDealerName()));
            }
            
            // Validate: If updating to non-admin role, dealer is required
            com.evdealer.enums.UserType newUserType = userUpdateRequest.getUserType() != null ? userUpdateRequest.getUserType() : user.getUserType();
            if (newUserType != com.evdealer.enums.UserType.ADMIN && dealer == null && user.getDealer() == null) {
                throw new RuntimeException("Dealer is required for non-admin users. Please provide dealerId or dealerName");
            }
            
            if (dealer != null) {
                user.setDealer(dealer);
            }
        }
        
        // Validate dealer requirement after update if user type changed to non-admin
        if (userUpdateRequest.getUserType() != null && userUpdateRequest.getUserType() != com.evdealer.enums.UserType.ADMIN) {
            if (user.getDealer() == null) {
                throw new RuntimeException("Dealer is required for non-admin users. Please provide dealerId or dealerName");
            }
        }
        
        if (userUpdateRequest.getIsActive() != null) {
            user.setIsActive(userUpdateRequest.getIsActive());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        try {
            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete user: " + e.getMessage() + ". User may be referenced by other records.");
        }
    }
    
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    // Password Management methods
    public String resetUserPassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        String passwordToSet = (newPassword != null && !newPassword.trim().isEmpty()) 
                ? newPassword 
                : "password123"; // Default password
        
        String hashedPassword = passwordEncoder.encode(passwordToSet);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        return "Password reset successfully for user: " + user.getUsername() + 
               (newPassword != null ? " with custom password" : " with default password");
    }
    
    public String resetUserPasswordByUsername(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        String passwordToSet = (newPassword != null && !newPassword.trim().isEmpty()) 
                ? newPassword 
                : "password123"; // Default password
        
        String hashedPassword = passwordEncoder.encode(passwordToSet);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        return "Password reset successfully for user: " + username + 
               (newPassword != null ? " with custom password" : " with default password");
    }
    
    public String resetUserPasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        String passwordToSet = (newPassword != null && !newPassword.trim().isEmpty()) 
                ? newPassword 
                : "password123"; // Default password
        
        String hashedPassword = passwordEncoder.encode(passwordToSet);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        return "Password reset successfully for user: " + user.getUsername() + 
               " (email: " + email + ")" +
               (newPassword != null ? " with custom password" : " with default password");
    }
    
    public Map<String, Object> bulkResetPasswords(List<UUID> userIds) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        
        for (UUID userId : userIds) {
            try {
                String message = resetUserPassword(userId, null); // Use default password
                successList.add(message);
            } catch (Exception e) {
                errorList.add("Failed to reset password for user ID " + userId + ": " + e.getMessage());
            }
        }
        
        result.put("totalRequested", userIds.size());
        result.put("successful", successList.size());
        result.put("failed", errorList.size());
        result.put("successList", successList);
        result.put("errorList", errorList);
        
        return result;
    }
}

