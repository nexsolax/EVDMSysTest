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
  LogOut,
  User,
  ShoppingCart,
  Truck,
  CreditCard,
  UserCircle
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import './Sidebar.css';

const Sidebar = ({ isOpen, onClose }) => {
  const { user, logout } = useAuth();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
  };

  const menuItems = [
    {
      path: '/dashboard',
      icon: LayoutDashboard,
      label: 'Tổng quan',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/vehicles',
      icon: Car,
      label: 'Quản lý xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff'],
      submenu: [
        { path: '/vehicles/brands', label: 'Thương hiệu' },
        { path: '/vehicles/models', label: 'Dòng xe' },
        { path: '/vehicles/variants', label: 'Phiên bản' },
        { path: '/vehicles/colors', label: 'Màu sắc' }
      ]
    },
    {
      path: '/customers',
      icon: Users,
      label: 'Khách hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/quotations',
      icon: FileText,
      label: 'Báo giá',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/orders',
      icon: ShoppingCart,
      label: 'Đơn hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/contracts',
      icon: FileText,
      label: 'Hợp đồng',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/deliveries',
      icon: Truck,
      label: 'Giao xe',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/payments',
      icon: CreditCard,
      label: 'Thanh toán',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/inventory',
      icon: Package,
      label: 'Kho hàng',
      roles: ['admin', 'evm_staff', 'dealer_manager'],
      submenu: [
        { path: '/inventory/warehouses', label: 'Kho' },
        { path: '/inventory/vehicles', label: 'Tồn kho xe' }
      ]
    },
    {
      path: '/reports',
      icon: BarChart3,
      label: 'Báo cáo',
      roles: ['admin', 'evm_staff', 'dealer_manager']
    },
    {
      path: '/users',
      icon: User,
      label: 'Người dùng',
      roles: ['admin', 'evm_staff']
    },
    {
      path: '/profile',
      icon: UserCircle,
      label: 'Thông tin cá nhân',
      roles: ['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']
    },
    {
      path: '/settings',
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
          {filteredMenuItems.map((item) => (
            <div key={item.path} className="nav-item">
              <NavLink
                to={item.path}
                className={({ isActive }) => 
                  `nav-link ${isActive ? 'active' : ''}`
                }
                onClick={onClose}
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
                      onClick={onClose}
                    >
                      {subItem.label}
                    </NavLink>
                  ))}
                </div>
              )}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">
              <User size={20} />
            </div>
            <div className="user-details">
              <div className="user-name">{user?.username}</div>
              <div className="user-role">{user?.role}</div>
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
