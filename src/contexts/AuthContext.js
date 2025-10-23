import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(null);

  useEffect(() => {
    // Check for existing token on app load
    const storedToken = localStorage.getItem('accessToken');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
      // Validate token with backend
      validateToken();
    } else {
      setLoading(false);
    }
  }, []);

  const validateToken = async () => {
    try {
      const response = await authAPI.validate();
      if (response.data) {
        setLoading(false);
      }
    } catch (error) {
      console.error('Token validation failed:', error);
      logout();
    }
  };

  const login = async (credentials) => {
    try {
      setLoading(true);
      const response = await authAPI.login(credentials);
      const { accessToken, userId, username, role } = response.data;
      
      // Map backend role to frontend role format
      const roleMapping = {
        'ADMIN': 'admin',
        'EVM_STAFF': 'evm_staff', 
        'DEALER_MANAGER': 'dealer_manager',
        'DEALER_STAFF': 'dealer_staff'
      };
      
      const userData = {
        userId,
        username,
        role: roleMapping[role] || role.toLowerCase(),
        originalRole: role, // Keep original role for API calls
      };

      setToken(accessToken);
      setUser(userData);
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('user', JSON.stringify(userData));
      
      setLoading(false);
      return { success: true, data: response.data };
    } catch (error) {
      setLoading(false);
      return { 
        success: false, 
        error: error.response?.data?.error || 'Đăng nhập thất bại' 
      };
    }
  };

  const register = async (userData) => {
    try {
      setLoading(true);
      const response = await authAPI.register(userData);
      setLoading(false);
      return { success: true, data: response.data };
    } catch (error) {
      setLoading(false);
      return { 
        success: false, 
        error: error.response?.data?.error || 'Đăng ký thất bại' 
      };
    }
  };

  const logout = async () => {
    try {
      if (token) {
        await authAPI.logout();
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      setToken(null);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
    }
  };

  const isAuthenticated = () => {
    return !!token && !!user;
  };

  const hasRole = (requiredRole) => {
    return user?.role === requiredRole;
  };

  const hasAnyRole = (roles) => {
    return roles.includes(user?.role);
  };

  // New role-based utility functions
  const isAdmin = () => {
    return user?.role === 'admin';
  };

  const isEvmStaff = () => {
    return user?.role === 'evm_staff';
  };

  const isDealerManager = () => {
    return user?.role === 'dealer_manager';
  };

  const isDealerStaff = () => {
    return user?.role === 'dealer_staff';
  };

  const canManageDealers = () => {
    return ['admin', 'evm_staff'].includes(user?.role);
  };

  const canManagePricing = () => {
    return ['admin', 'evm_staff'].includes(user?.role);
  };

  const canViewReports = () => {
    return ['admin', 'evm_staff', 'dealer_manager'].includes(user?.role);
  };

  const canManageUsers = () => {
    return ['admin', 'evm_staff'].includes(user?.role);
  };

  const canManageInventory = () => {
    return ['admin', 'evm_staff', 'dealer_manager'].includes(user?.role);
  };

  const getRoleDisplayName = () => {
    const roleNames = {
      'admin': 'Quản trị viên',
      'evm_staff': 'Nhân viên EVM',
      'dealer_manager': 'Quản lý đại lý',
      'dealer_staff': 'Nhân viên đại lý'
    };
    return roleNames[user?.role] || user?.role;
  };

  const getRolePermissions = () => {
    const permissions = {
      'admin': [
        'manage_users', 'manage_dealers', 'manage_pricing', 'view_reports',
        'manage_inventory', 'manage_appointments', 'manage_promotions',
        'manage_feedbacks', 'manage_vehicles', 'manage_customers',
        'manage_quotations', 'manage_orders', 'manage_contracts',
        'manage_deliveries', 'manage_payments'
      ],
      'evm_staff': [
        'manage_dealers', 'manage_pricing', 'view_reports', 'manage_inventory',
        'manage_appointments', 'manage_promotions', 'manage_feedbacks',
        'manage_vehicles', 'manage_customers', 'manage_quotations',
        'manage_orders', 'manage_contracts', 'manage_deliveries', 'manage_payments'
      ],
      'dealer_manager': [
        'view_reports', 'manage_inventory', 'manage_appointments',
        'manage_promotions', 'manage_feedbacks', 'manage_vehicles',
        'manage_customers', 'manage_quotations', 'manage_orders',
        'manage_contracts', 'manage_deliveries', 'manage_payments'
      ],
      'dealer_staff': [
        'manage_appointments', 'manage_feedbacks', 'manage_vehicles',
        'manage_customers', 'manage_quotations', 'manage_orders',
        'manage_contracts', 'manage_deliveries', 'manage_payments'
      ]
    };
    return permissions[user?.role] || [];
  };

  const hasPermission = (permission) => {
    return getRolePermissions().includes(permission);
  };

  const value = {
    user,
    token,
    loading,
    login,
    register,
    logout,
    isAuthenticated,
    hasRole,
    hasAnyRole,
    // New role-based functions
    isAdmin,
    isEvmStaff,
    isDealerManager,
    isDealerStaff,
    canManageDealers,
    canManagePricing,
    canViewReports,
    canManageUsers,
    canManageInventory,
    getRoleDisplayName,
    getRolePermissions,
    hasPermission,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
