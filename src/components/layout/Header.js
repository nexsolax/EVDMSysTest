import React, { useState, useEffect, useRef } from 'react';
import { Menu, Bell, Search, UserCircle, LogOut, Settings, ChevronDown } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import './Header.css';

const Header = ({ onMenuClick, title }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  // State for dropdowns and notifications
  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  
  // Refs for click outside detection
  const notificationRef = useRef(null);
  const userMenuRef = useRef(null);

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

  // Load notifications only if user is logged in
  useEffect(() => {
    if (user) {
      loadNotifications();
    }
  }, [user]);

  // Click outside to close dropdowns
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false);
      }
      if (userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setShowUserMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const loadNotifications = async () => {
    try {
      // Mock notifications for now - replace with actual API call
      const mockNotifications = [
        {
          id: 1,
          title: 'Đơn hàng mới',
          message: 'Bạn có đơn hàng mới #ORD001 cần xử lý',
          type: 'order',
          isRead: false,
          createdAt: new Date().toISOString()
        },
        {
          id: 2,
          title: 'Thanh toán thành công',
          message: 'Khách hàng đã thanh toán đơn hàng #ORD002',
          type: 'payment',
          isRead: false,
          createdAt: new Date(Date.now() - 3600000).toISOString()
        },
        {
          id: 3,
          title: 'Giao xe hoàn tất',
          message: 'Xe đã được giao thành công cho khách hàng',
          type: 'delivery',
          isRead: true,
          createdAt: new Date(Date.now() - 7200000).toISOString()
        }
      ];
      
      setNotifications(mockNotifications);
      setUnreadCount(mockNotifications.filter(n => !n.isRead).length);
    } catch (error) {
      console.error('Error loading notifications:', error);
    }
  };

  const handleNotificationClick = (notification) => {
    // Mark as read
    const updatedNotifications = notifications.map(n => 
      n.id === notification.id ? { ...n, isRead: true } : n
    );
    setNotifications(updatedNotifications);
    setUnreadCount(updatedNotifications.filter(n => !n.isRead).length);
    
    // Navigate based on notification type
    switch (notification.type) {
      case 'order':
        navigate('/admin/orders');
        break;
      case 'payment':
        navigate('/admin/payments');
        break;
      case 'delivery':
        navigate('/admin/deliveries');
        break;
      default:
        break;
    }
    
    setShowNotifications(false);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // Implement global search functionality
      console.log('Searching for:', searchQuery);
      // You can implement a global search modal or redirect to search results
    }
  };

  const handleLogout = () => {
    if (logout) {
      logout();
      navigate('/login');
    }
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
        <form className="search-container" onSubmit={handleSearch}>
          <Search className="search-icon" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm..."
            className="search-input"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </form>
      </div>

      <div className="header-right">
        {user ? (
          <>
            <div className="notification-container" ref={notificationRef}>
              <button 
                className="notification-button"
                onClick={() => setShowNotifications(!showNotifications)}
              >
                <Bell size={20} />
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount}</span>
                )}
              </button>
              
              {showNotifications && (
                <div className="notification-dropdown">
                  <div className="notification-header">
                    <h3>Thông báo</h3>
                    <span className="notification-count">{unreadCount} chưa đọc</span>
                  </div>
                  <div className="notification-list">
                    {notifications.length === 0 ? (
                      <div className="notification-empty">
                        <p>Không có thông báo nào</p>
                      </div>
                    ) : (
                      notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                          onClick={() => handleNotificationClick(notification)}
                        >
                          <div className="notification-content">
                            <h4>{notification.title}</h4>
                            <p>{notification.message}</p>
                            <span className="notification-time">
                              {new Date(notification.createdAt).toLocaleString('vi-VN')}
                            </span>
                          </div>
                          {!notification.isRead && <div className="unread-dot"></div>}
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
            
            <div className="user-menu" ref={userMenuRef}>
              <div 
                className="user-info" 
                onClick={() => setShowUserMenu(!showUserMenu)}
              >
                <div className="user-avatar">
                  {user?.username?.charAt(0).toUpperCase()}
                </div>
                <div className="user-details">
                  <div className="user-name">{user?.username}</div>
                  <div className="user-role">{user?.role}</div>
                </div>
                <ChevronDown size={16} className="user-chevron" />
              </div>
              
              {showUserMenu && (
                <div className="user-dropdown">
                  <button 
                    className="dropdown-item"
                    onClick={() => {
                      navigate('/admin/profile');
                      setShowUserMenu(false);
                    }}
                  >
                    <UserCircle size={16} />
                    <span>Thông tin cá nhân</span>
                  </button>
                  <button 
                    className="dropdown-item"
                    onClick={() => {
                      navigate('/admin/settings');
                      setShowUserMenu(false);
                    }}
                  >
                    <Settings size={16} />
                    <span>Cài đặt</span>
                  </button>
                  <div className="dropdown-divider"></div>
                  <button 
                    className="dropdown-item logout"
                    onClick={handleLogout}
                  >
                    <LogOut size={16} />
                    <span>Đăng xuất</span>
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="login-actions">
            <button 
              className="login-button"
              onClick={() => navigate('/login')}
            >
              Đăng nhập
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
