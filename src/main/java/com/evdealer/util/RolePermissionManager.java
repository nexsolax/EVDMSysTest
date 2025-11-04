package com.evdealer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class để quản lý permissions cho roles
 */
public class RolePermissionManager {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    private static final MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, List.class);
    
    /**
     * Tạo permissions JSON string cho role
     */
    public static String createPermissionsForRole(String roleName) {
        Map<String, List<String>> permissions = new HashMap<>();
        
        switch (roleName.toUpperCase()) {
            case "ADMIN":
                permissions = createAdminPermissions();
                break;
            case "DEALER_MANAGER":
                permissions = createDealerManagerPermissions();
                break;
            case "DEALER_STAFF":
                permissions = createDealerStaffPermissions();
                break;
            case "EVM_STAFF":
                permissions = createEVMStaffPermissions();
                break;
            case "CUSTOMER":
                permissions = createCustomerPermissions();
                break;
            default:
                permissions = createDefaultPermissions();
        }
        
        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    
    /**
     * Kiểm tra xem role có permission cụ thể không
     */
    public static boolean hasPermission(String permissionsJson, String module, String action) {
        try {
            Map<String, List<String>> permissions = objectMapper.readValue(permissionsJson, mapType);
            List<String> modulePermissions = permissions.get(module);
            return modulePermissions != null && modulePermissions.contains(action);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Lấy danh sách permissions của role
     */
    public static List<String> getRolePermissions(String permissionsJson) {
        try {
            Map<String, List<String>> permissions = objectMapper.readValue(permissionsJson, mapType);
            return permissions.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(action -> entry.getKey() + ":" + action))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    // Admin có tất cả quyền
    private static Map<String, List<String>> createAdminPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("users", Arrays.asList("read", "write", "delete"));
        permissions.put("dealers", Arrays.asList("read", "write", "delete"));
        permissions.put("customers", Arrays.asList("read", "write", "delete"));
        permissions.put("vehicles", Arrays.asList("read", "write", "delete"));
        permissions.put("orders", Arrays.asList("read", "write", "delete"));
        permissions.put("quotations", Arrays.asList("read", "write", "delete"));
        permissions.put("reports", Arrays.asList("read", "export"));
        permissions.put("settings", Arrays.asList("read", "write"));
        return permissions;
    }
    
    // Dealer Manager quản lý dealer và các hoạt động liên quan
    private static Map<String, List<String>> createDealerManagerPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("dealers", Arrays.asList("read", "write"));
        permissions.put("customers", Arrays.asList("read", "write", "delete"));
        permissions.put("vehicles", Arrays.asList("read", "write", "delete"));
        permissions.put("orders", Arrays.asList("read", "write", "delete"));
        permissions.put("quotations", Arrays.asList("read", "write", "delete"));
        permissions.put("reports", Arrays.asList("read", "export"));
        return permissions;
    }
    
    // Dealer Staff xử lý khách hàng và đơn hàng
    private static Map<String, List<String>> createDealerStaffPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("customers", Arrays.asList("read", "write"));
        permissions.put("vehicles", Arrays.asList("read", "write"));
        permissions.put("orders", Arrays.asList("read", "write"));
        permissions.put("quotations", Arrays.asList("read", "write"));
        return permissions;
    }
    
    // EVM Staff quản lý xe và báo cáo
    private static Map<String, List<String>> createEVMStaffPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("dealers", Arrays.asList("read"));
        permissions.put("customers", Arrays.asList("read"));
        permissions.put("vehicles", Arrays.asList("read", "write"));
        permissions.put("orders", Arrays.asList("read"));
        permissions.put("quotations", Arrays.asList("read"));
        permissions.put("reports", Arrays.asList("read", "export"));
        return permissions;
    }
    
    // Customer chỉ xem thông tin
    private static Map<String, List<String>> createCustomerPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("vehicles", Arrays.asList("read"));
        permissions.put("orders", Arrays.asList("read"));
        permissions.put("quotations", Arrays.asList("read"));
        return permissions;
    }
    
    // Default permissions (read-only)
    private static Map<String, List<String>> createDefaultPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("read", Arrays.asList("read"));
        return permissions;
    }
}
