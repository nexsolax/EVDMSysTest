package com.evdealer.util;

import com.evdealer.entity.User;
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
                if (userId != null) {
                    return Optional.of(userId);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Try to get from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String username = (String) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return Optional.of(user.get().getUserId().toString());
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
     * Lấy user role hiện tại
     */
    public Optional<String> getCurrentUserRole() {
        // Try from request attribute first
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String role = (String) request.getAttribute("userRole");
                if (role != null) {
                    return Optional.of(role);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Try from SecurityContext authorities
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.startsWith("ROLE_")) {
                    return Optional.of(role.substring(5)); // Remove "ROLE_" prefix
                }
            }
        }
        
        // Try from User entity
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isPresent() && userOpt.get().getUserType() != null) {
            return Optional.of(userOpt.get().getUserType().toString());
        }
        
        return Optional.empty();
    }
    
    /**
     * Kiểm tra user hiện tại có role cụ thể không
     */
    public boolean hasRole(String role) {
        Optional<String> currentRole = getCurrentUserRole();
        return currentRole.isPresent() && currentRole.get().equalsIgnoreCase(role);
    }
    
    /**
     * Kiểm tra user hiện tại có một trong các roles không
     */
    public boolean hasAnyRole(String... roles) {
        Optional<String> currentRole = getCurrentUserRole();
        if (currentRole.isPresent()) {
            for (String role : roles) {
                if (currentRole.get().equalsIgnoreCase(role)) {
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
        return hasRole("ADMIN");
    }
    
    /**
     * Kiểm tra user hiện tại có phải EVM_STAFF không
     */
    public boolean isEvmStaff() {
        return hasRole("EVM_STAFF");
    }
    
    /**
     * Kiểm tra user hiện tại có phải DEALER_MANAGER hoặc DEALER_STAFF không
     */
    public boolean isDealerUser() {
        return hasAnyRole("DEALER_MANAGER", "DEALER_STAFF");
    }
    
    /**
     * Kiểm tra user hiện tại có phải DEALER_MANAGER không
     */
    public boolean isDealerManager() {
        return hasRole("DEALER_MANAGER");
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

