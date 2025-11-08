package com.evdealer.util;

import com.evdealer.entity.User;
import com.evdealer.enums.Role;
import com.evdealer.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class để lấy thông tin user hiện tại từ SecurityContext hoặc Request
 */
@Component
public class SecurityUtils {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy userId từ SecurityContext hoặc request attribute
     */
    public Optional<String> getCurrentUserId() {
        // Try to get from request attribute first (set by JwtAuthenticationFilter)
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = (String) request.getAttribute("userId");
                if (userId != null && !userId.isEmpty()) {
                    return Optional.of(userId);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Try to get from SecurityContext - get username then find user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String username = (String) authentication.getPrincipal();
            try {
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    return Optional.of(user.get().getUserId().toString());
                }
            } catch (Exception e) {
                // Log error but continue
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Lấy User entity hiện tại
     */
    public Optional<User> getCurrentUser() {
        Optional<String> userIdOpt = getCurrentUserId();
        if (userIdOpt.isPresent()) {
            try {
                UUID userId = UUID.fromString(userIdOpt.get());
                // Try to get user with dealer eagerly loaded
                Optional<User> userWithDealer = userRepository.findByIdWithDealer(userId);
                if (userWithDealer.isPresent()) {
                    return userWithDealer;
                }
                // Fallback to regular findById
                return userRepository.findById(userId);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    /**
     * Lấy username hiện tại
     */
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }
        
        // Try from request attribute
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String username = (String) request.getAttribute("username");
                if (username != null) {
                    return Optional.of(username);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return Optional.empty();
    }
    
    /**
     * Lấy user role hiện tại (normalized string)
     */
    public Optional<String> getCurrentUserRole() {
        Optional<Role> roleOpt = getCurrentUserRoleEnum();
        return roleOpt.map(role -> role.getValue());
    }
    
    /**
     * Lấy user role hiện tại (Role enum)
     */
    public Optional<Role> getCurrentUserRoleEnum() {
        // Try from request attribute first
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String roleStr = (String) request.getAttribute("userRole");
                if (roleStr != null) {
                    Role role = Role.fromString(roleStr);
                    if (role != null) {
                        return Optional.of(role);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Try from SecurityContext authorities
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String authorityStr = authority.getAuthority();
                Role role = Role.fromString(authorityStr);
                if (role != null) {
                    return Optional.of(role);
                }
            }
        }
        
        // Try from User entity
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isPresent() && userOpt.get().getUserType() != null) {
            Role role = Role.fromString(userOpt.get().getUserType().toString());
            if (role != null) {
                return Optional.of(role);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Kiểm tra user hiện tại có role cụ thể không (string - backward compatible)
     */
    public boolean hasRole(String role) {
        Role roleEnum = Role.fromString(role);
        if (roleEnum == null) {
            return false;
        }
        return hasRole(roleEnum);
    }
    
    /**
     * Kiểm tra user hiện tại có role cụ thể không (enum - recommended)
     */
    public boolean hasRole(Role role) {
        Optional<Role> currentRole = getCurrentUserRoleEnum();
        return currentRole.isPresent() && currentRole.get() == role;
    }
    
    /**
     * Kiểm tra user hiện tại có một trong các roles không (string - backward compatible)
     */
    public boolean hasAnyRole(String... roles) {
        Optional<Role> currentRole = getCurrentUserRoleEnum();
        if (currentRole.isPresent()) {
            for (String roleStr : roles) {
                Role role = Role.fromString(roleStr);
                if (role != null && currentRole.get() == role) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra user hiện tại có một trong các roles không (enum - recommended)
     */
    public boolean hasAnyRole(Role... roles) {
        Optional<Role> currentRole = getCurrentUserRoleEnum();
        if (currentRole.isPresent()) {
            for (Role role : roles) {
                if (currentRole.get() == role) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra user hiện tại có phải ADMIN không
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    /**
     * Kiểm tra user hiện tại có phải EVM_STAFF không
     */
    public boolean isEvmStaff() {
        return hasRole(Role.EVM_STAFF);
    }
    
    /**
     * Kiểm tra user hiện tại có phải DEALER_MANAGER không
     */
    public boolean isDealerUser() {
        return hasRole(Role.DEALER_MANAGER);
    }
    
    /**
     * Kiểm tra user hiện tại có phải DEALER_MANAGER không
     */
    public boolean isDealerManager() {
        return hasRole(Role.DEALER_MANAGER);
    }
    
    /**
     * Kiểm tra user hiện tại có thuộc dealer cụ thể không
     */
    public boolean belongsToDealer(UUID dealerId) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getDealer() != null && user.getDealer().getDealerId().equals(dealerId);
        }
        return false;
    }
}

