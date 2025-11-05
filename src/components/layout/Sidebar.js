import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Car, 
  Users, 
  FileText, 
  Package, 
  BarChart3, 
  Settings,
  Shield,
  LogOut,
  User,
  ShoppingCart,
  Truck,
  CreditCard,
  UserCircle,
  Gift,
  Calendar,
  MessageSquare,
  Building2,
  DollarSign,
  Target,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import '../../styles/layout.css';

const Sidebar = ({ isOpen, onClose }) => {
  const { user, logout, getRoleDisplayName } = useAuth();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
  };

  const menuItems = [
    {
      path: '/admin/dashboard',
      icon: LayoutDashboard,
      label: 'Tổng quan',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/vehicle-management',
      icon: Car,
      label: 'Danh sách xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/vehicles',
      icon: Car,
      label: 'Quản lý xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff'],
      submenu: [
        { path: '/admin/vehicles/brands', label: 'Thương hiệu' },
        { path: '/admin/vehicles/models', label: 'Dòng xe' },
        { path: '/admin/vehicles/variants', label: 'Phiên bản' },
        { path: '/admin/vehicles/colors', label: 'Màu sắc' },
        { path: '/admin/vehicle-comparison', label: 'So sánh xe' }
      ]
    },
    {
      path: '/admin/customers',
      icon: Users,
      label: 'Khách hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/quotations',
      icon: FileText,
      label: 'Báo giá từ khách',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/dealer-quotations',
      icon: FileText,
      label: 'Báo giá từ đại lý',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/dealer-quotation-requests',
      icon: FileText,
      label: 'Yêu cầu báo giá của tôi',
      roles: ['dealer_manager', 'dealer_staff', 'admin']
    },
    {
      path: '/admin/orders',
      icon: ShoppingCart,
      label: 'Đơn hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/dealer-orders',
      icon: ShoppingCart,
      label: 'Đại lý đặt xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/dealer-invoices',
      icon: FileText,
      label: 'Hóa đơn đại lý',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/dealer-payments',
      icon: CreditCard,
      label: 'Thanh toán đại lý',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/contracts',
      icon: FileText,
      label: 'Hợp đồng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/deliveries',
      icon: Truck,
      label: 'Giao xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/payments',
      icon: CreditCard,
      label: 'Thanh toán',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/promotions',
      icon: Gift,
      label: 'Khuyến mãi',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/appointments',
      icon: Calendar,
      label: 'Lịch hẹn lái thử',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/feedbacks',
      icon: MessageSquare,
      label: 'Phản hồi & Khiếu nại',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/inventory',
      icon: Package,
      label: 'Kho hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager'],
      submenu: [
        { path: '/admin/inventory/warehouses', label: 'Kho' },
        { path: '/admin/inventory/vehicles', label: 'Tồn kho xe' }
      ]
    },
    {
      path: '/admin/vehicle-images',
      icon: Car,
      label: 'Hình ảnh xe',
      roles: ['admin', 'evm_staff']
    },
    {
      path: '/admin/reports',
      icon: BarChart3,
      label: 'Báo cáo',
      roles: ['admin', 'evm_staff', 'dealer_manager']
    },
    {
      path: '/admin/dealers',
      icon: Building2,
      label: 'Quản lý đại lý',
      roles: ['admin', 'evm_staff']
    },
    {
      path: '/admin/pricing',
      icon: DollarSign,
      label: 'Giá sỉ & Chiết khấu',
      roles: ['admin', 'evm_staff']
    },
    {
      path: '/admin/installment-plans',
      icon: CreditCard,
      label: 'Kế hoạch trả góp',
      roles: ['admin', 'evm_staff', 'dealer_manager']
    },
    {
      path: '/admin/dealer-targets',
      icon: Target,
      label: 'Mục tiêu đại lý',
      roles: ['admin', 'evm_staff']
    },
    {
      path: '/admin/users',
      icon: User,
      label: 'Người dùng',
      roles: ['admin', 'evm_staff']
    },
    // Role Management - DISABLED: API đã bị xóa, hệ thống dùng UserType enum
    // {
    //   path: '/admin/roles',
    //   icon: Shield,
    //   label: 'Vai trò',
    //   roles: ['admin']
    // },
    {
      path: '/admin/profile',
      icon: UserCircle,
      label: 'Thông tin cá nhân',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/admin/settings',
      icon: Settings,
      label: 'Cài đặt',
      roles: ['admin']
    }
  ];

  const filteredMenuItems = menuItems.filter(item => 
    item.roles.includes(user?.role)
  );

  return (
    <>
      {isOpen && <div className="sidebar-overlay" onClick={onClose} />}
      <div className={`sidebar ${isOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <Car className="logo-icon" />
            <span className="logo-text">EV Dealer</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {filteredMenuItems.map((item) => {
            // Check if parent item or any submenu item is active
            const isParentActive = location.pathname === item.path || 
              (item.submenu && item.submenu.some(subItem => location.pathname === subItem.path));
            
            return (
              <div key={item.path} className="nav-item">
                <NavLink
                  to={item.path}
                  className={({ isActive }) => 
                    `nav-link ${isActive || isParentActive ? 'active' : ''}`
                  }
                  onClick={(e) => {
                    // Don't prevent default - let navigation happen
                    if (onClose) {
                      onClose();
                    }
                  }}
                  end={!item.submenu} // Use exact match for items without submenu
                >
                  <item.icon className="nav-icon" />
                  <span className="nav-label">{item.label}</span>
                </NavLink>
                
                {item.submenu && (
                  <div className="submenu">
                    {item.submenu.map((subItem) => (
                      <NavLink
                        key={subItem.path}
                        to={subItem.path}
                        className={({ isActive }) => 
                          `submenu-link ${isActive ? 'active' : ''}`
                        }
                        onClick={(e) => {
                          // Ensure navigation happens
                          if (onClose) {
                            onClose();
                          }
                        }}
                        end
                      >
                        {subItem.label}
                      </NavLink>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">
              <User size={20} />
            </div>
            <div className="user-details">
              <div className="user-name">{user?.username}</div>
              <div className="user-role">{getRoleDisplayName()}</div>
            </div>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            <LogOut size={20} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>
    </>
  );
};

export default Sidebar;

