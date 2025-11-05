package com.evdealer.controller;

import com.evdealer.dto.UserRequest;
import com.evdealer.dto.UserUpdateRequest;
import com.evdealer.entity.User;
import com.evdealer.service.UserService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "APIs quản lý người dùng")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    // User endpoints
    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng", description = "Lấy tất cả người dùng trong hệ thống")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Lấy người dùng đang hoạt động", description = "Lấy danh sách người dùng đang hoạt động")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Lấy người dùng theo ID", description = "Lấy thông tin người dùng theo ID")
    public ResponseEntity<User> getUserById(@PathVariable @Parameter(description = "User ID") UUID userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    // Remove password hash for security
                    user.setPasswordHash(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin user hiện tại", description = "Lấy thông tin user đang đăng nhập")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Optional<User> userOpt = securityUtils.getCurrentUser();
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Remove password hash for security
                user.setPasswordHash(null);
                return ResponseEntity.ok(user);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found or not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get current user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/username/{username}")
    @Operation(summary = "Lấy người dùng theo tên đăng nhập", description = "Lấy thông tin người dùng theo tên đăng nhập")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> {
                    // Remove password hash for security
                    user.setPasswordHash(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Lấy người dùng theo email", description = "Lấy thông tin người dùng theo email")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> {
                    // Remove password hash for security
                    user.setPasswordHash(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/role/{roleName}")
    @Operation(summary = "Lấy người dùng theo vai trò", description = "Lấy danh sách người dùng theo vai trò")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String roleName) {
        List<User> users = userService.getUsersByRole(roleName);
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy người dùng theo đại lý", description = "Lấy danh sách người dùng thuộc một đại lý cụ thể")
    public ResponseEntity<?> getUsersByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem users của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view users for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<User> users = userService.getUsersByDealer(dealerId);
            // Remove password hash from all users for security
            users.forEach(user -> user.setPasswordHash(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/role-string/{roleString}")
    @Operation(summary = "Lấy người dùng theo role string", description = "Lấy danh sách người dùng theo role string (DEALER_STAFF, DEALER_MANAGER, EVM_STAFF, ADMIN)")
    public ResponseEntity<List<User>> getUsersByRoleString(@PathVariable String roleString) {
        List<User> users = userService.getUsersByRoleString(roleString);
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/dealer-staff")
    @Operation(summary = "Lấy nhân viên đại lý", description = "Lấy danh sách tất cả nhân viên đại lý")
    public ResponseEntity<List<User>> getDealerStaff() {
        List<User> users = userService.getUsersByRoleString("DEALER_STAFF");
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/dealer-managers")
    @Operation(summary = "Lấy quản lý đại lý", description = "Lấy danh sách tất cả quản lý đại lý")
    public ResponseEntity<List<User>> getDealerManagers() {
        List<User> users = userService.getUsersByRoleString("DEALER_MANAGER");
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/evm-staff")
    @Operation(summary = "Lấy nhân viên EVM", description = "Lấy danh sách tất cả nhân viên EVM")
    public ResponseEntity<List<User>> getEvmStaff() {
        List<User> users = userService.getUsersByRoleString("EVM_STAFF");
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/admins")
    @Operation(summary = "Lấy quản trị viên", description = "Lấy danh sách tất cả quản trị viên")
    public ResponseEntity<List<User>> getAdmins() {
        List<User> users = userService.getUsersByRoleString("ADMIN");
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm người dùng", description = "Tìm kiếm người dùng theo tên")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        List<User> users = userService.searchUsersByName(name);
        // Remove password hash from all users for security
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    @Operation(summary = "Tạo người dùng mới", description = "Tạo người dùng mới")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo user
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create users");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            User createdUser = userService.createUser(user);
            // Remove password hash from response for security
            createdUser.setPasswordHash(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/dto")
    @Operation(summary = "Tạo người dùng mới từ DTO", description = "Tạo người dùng mới từ UserRequest DTO")
    public ResponseEntity<?> createUserFromRequest(@RequestBody UserRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo user
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create users");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            User createdUser = userService.createUserFromRequest(request);
            // Remove password hash from response for security
            createdUser.setPasswordHash(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{userId}")
    @Operation(summary = "Cập nhật người dùng", description = "Cập nhật thông tin người dùng")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc user tự update chính mình
            var currentUserOpt = securityUtils.getCurrentUser();
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                // User chỉ có thể update chính mình
                if (currentUserOpt.isPresent()) {
                    UUID currentUserId = currentUserOpt.get().getUserId();
                    if (!userId.equals(currentUserId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update your own profile");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or the user themselves can update users");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            User updatedUser = userService.updateUser(userId, userUpdateRequest);
            // Remove password hash from response for security
            updatedUser.setPasswordHash(null);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "Xóa người dùng", description = "Xóa người dùng")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa user
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete users");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User deletion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Vô hiệu hóa người dùng", description = "Vô hiệu hóa người dùng")
    public ResponseEntity<?> deactivateUser(@PathVariable UUID userId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể deactivate user
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can deactivate users");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            userService.deactivateUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Password Management endpoints
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Đặt lại mật khẩu người dùng", description = "Quản trị viên có thể đặt lại mật khẩu cho người dùng")
    public ResponseEntity<?> resetUserPassword(
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @RequestParam(required = false) String newPassword) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể reset password
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can reset passwords");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String result = userService.resetUserPassword(userId, newPassword);
            return ResponseEntity.ok().body(java.util.Map.of("message", result));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/username/{username}/reset-password")
    @Operation(summary = "Đặt lại mật khẩu theo tên đăng nhập", description = "Quản trị viên có thể đặt lại mật khẩu theo tên đăng nhập")
    public ResponseEntity<?> resetUserPasswordByUsername(
            @PathVariable @Parameter(description = "Username") String username,
            @RequestParam(required = false) String newPassword) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể reset password
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can reset passwords");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String result = userService.resetUserPasswordByUsername(username, newPassword);
            return ResponseEntity.ok().body(java.util.Map.of("message", result));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/email/{email}/reset-password")
    @Operation(summary = "Đặt lại mật khẩu theo email", description = "Quản trị viên có thể đặt lại mật khẩu theo email")
    public ResponseEntity<?> resetUserPasswordByEmail(
            @PathVariable @Parameter(description = "Email") String email,
            @RequestParam(required = false) String newPassword) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể reset password
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can reset passwords");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String result = userService.resetUserPasswordByEmail(email, newPassword);
            return ResponseEntity.ok().body(java.util.Map.of("message", result));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/bulk-reset-password")
    @Operation(summary = "Đặt lại mật khẩu hàng loạt", description = "Quản trị viên có thể đặt lại mật khẩu cho nhiều người dùng")
    public ResponseEntity<?> bulkResetPasswords(@RequestBody java.util.List<UUID> userIds) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể bulk reset password
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can bulk reset passwords");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            java.util.Map<String, Object> result = userService.bulkResetPasswords(userIds);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to bulk reset passwords: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

