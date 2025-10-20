import React from 'react';
import { Menu, Bell, Search, UserCircle } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import './Header.css';

const Header = ({ onMenuClick, title }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Get page title based on current route
  const getPageTitle = () => {
    if (title) return title;
    
    const path = location.pathname;
    const titleMap = {
      '/dashboard': 'Dashboard',
      '/vehicles': 'Quản lý xe',
      '/customers': 'Quản lý khách hàng',
      '/quotations': 'Báo giá',
      '/orders': 'Đơn hàng',
      '/contracts': 'Hợp đồng',
      '/deliveries': 'Giao xe',
      '/payments': 'Thanh toán',
      '/inventory': 'Kho hàng',
      '/reports': 'Báo cáo',
      '/users': 'Người dùng',
      '/profile': 'Thông tin cá nhân',
      '/settings': 'Cài đặt'
    };
    
    return titleMap[path] || 'Dashboard';
  };

  return (
    <header className="header">
      <div className="header-left">
        <button className="menu-button" onClick={onMenuClick}>
          <Menu size={24} />
        </button>
        <h1 className="header-title">{getPageTitle()}</h1>
      </div>

      <div className="header-center">
        <div className="search-container">
          <Search className="search-icon" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm..."
            className="search-input"
          />
        </div>
      </div>

      <div className="header-right">
        <button className="notification-button">
          <Bell size={20} />
          <span className="notification-badge">3</span>
        </button>
        
        <div className="user-menu">
          <div className="user-info" onClick={() => navigate('/profile')}>
            <div className="user-avatar">
              {user?.username?.charAt(0).toUpperCase()}
            </div>
            <div className="user-details">
              <div className="user-name">{user?.username}</div>
              <div className="user-role">{user?.role}</div>
            </div>
          </div>
          
          <button 
            className="profile-button"
            onClick={() => navigate('/profile')}
            title="Thông tin cá nhân"
          >
            <UserCircle size={20} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
