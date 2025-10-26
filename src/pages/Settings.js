import React, { useState } from 'react';
import { User, Bell, Shield, Palette, Globe, Database } from 'lucide-react';
import './Settings.css';

const Settings = () => {
  const [activeTab, setActiveTab] = useState('profile');

  const tabs = [
    { id: 'profile', label: 'Thông tin cá nhân', icon: User },
    { id: 'notifications', label: 'Thông báo', icon: Bell },
    { id: 'security', label: 'Bảo mật', icon: Shield },
    { id: 'appearance', label: 'Giao diện', icon: Palette },
    { id: 'language', label: 'Ngôn ngữ', icon: Globe },
    { id: 'data', label: 'Dữ liệu', icon: Database },
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return (
          <div className="settings-content">
            <h2>Thông tin cá nhân</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Tên đăng nhập</label>
                <input type="text" value="admin" readOnly />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value="admin@evdm.com" />
              </div>
              <div className="form-group">
                <label>Số điện thoại</label>
                <input type="tel" value="0123456789" />
              </div>
              <div className="form-group">
                <label>Họ và tên</label>
                <input type="text" value="Administrator" />
              </div>
              <button className="save-button">Lưu thay đổi</button>
            </div>
          </div>
        );
      
      case 'notifications':
        return (
          <div className="settings-content">
            <h2>Cài đặt thông báo</h2>
            <div className="settings-form">
              <div className="checkbox-group">
                <label>
                  <input type="checkbox" defaultChecked />
                  <span>Thông báo đơn hàng mới</span>
                </label>
                <label>
                  <input type="checkbox" defaultChecked />
                  <span>Thông báo thanh toán</span>
                </label>
                <label>
                  <input type="checkbox" defaultChecked />
                  <span>Thông báo giao xe</span>
                </label>
                <label>
                  <input type="checkbox" />
                  <span>Thông báo email</span>
                </label>
              </div>
              <button className="save-button">Lưu cài đặt</button>
            </div>
          </div>
        );
      
      case 'security':
        return (
          <div className="settings-content">
            <h2>Bảo mật</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Mật khẩu hiện tại</label>
                <input type="password" />
              </div>
              <div className="form-group">
                <label>Mật khẩu mới</label>
                <input type="password" />
              </div>
              <div className="form-group">
                <label>Xác nhận mật khẩu mới</label>
                <input type="password" />
              </div>
              <button className="save-button">Đổi mật khẩu</button>
            </div>
          </div>
        );
      
      case 'appearance':
        return (
          <div className="settings-content">
            <h2>Giao diện</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Chủ đề</label>
                <select>
                  <option value="light">Sáng</option>
                  <option value="dark">Tối</option>
                  <option value="auto">Tự động</option>
                </select>
              </div>
              <div className="form-group">
                <label>Ngôn ngữ</label>
                <select>
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </div>
              <button className="save-button">Lưu cài đặt</button>
            </div>
          </div>
        );
      
      case 'language':
        return (
          <div className="settings-content">
            <h2>Ngôn ngữ và vùng</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Ngôn ngữ</label>
                <select>
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </div>
              <div className="form-group">
                <label>Múi giờ</label>
                <select>
                  <option value="Asia/Ho_Chi_Minh">Asia/Ho_Chi_Minh (GMT+7)</option>
                  <option value="UTC">UTC (GMT+0)</option>
                </select>
              </div>
              <button className="save-button">Lưu cài đặt</button>
            </div>
          </div>
        );
      
      case 'data':
        return (
          <div className="settings-content">
            <h2>Quản lý dữ liệu</h2>
            <div className="settings-form">
              <div className="data-actions">
                <button className="action-button export">
                  <Database size={20} />
                  <span>Xuất dữ liệu</span>
                </button>
                <button className="action-button import">
                  <Database size={20} />
                  <span>Nhập dữ liệu</span>
                </button>
                <button className="action-button backup">
                  <Database size={20} />
                  <span>Sao lưu dữ liệu</span>
                </button>
              </div>
            </div>
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className="settings-page">
      <div className="settings-container">
        <div className="settings-sidebar">
          <h1>Cài đặt</h1>
          <nav className="settings-nav">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  className={`nav-item ${activeTab === tab.id ? 'active' : ''}`}
                  onClick={() => setActiveTab(tab.id)}
                >
                  <Icon size={20} />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </nav>
        </div>
        
        <div className="settings-main">
          {renderTabContent()}
        </div>
      </div>
    </div>
  );
};

export default Settings;

