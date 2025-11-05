import React from 'react';
import { X, User, Mail, Phone, MapPin, Calendar, Shield, Clock } from 'lucide-react';
import './UserDetailModal.css';

const UserDetailModal = ({ user, isOpen, onClose }) => {
  if (!isOpen || !user) return null;

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const getRoleBadgeClass = (roleName) => {
    switch (roleName) {
      case 'admin':
        return 'badge-danger';
      case 'evm_staff':
        return 'badge-info';
      case 'dealer_manager':
        return 'badge-warning';
      case 'dealer_staff':
        return 'badge-success';
      default:
        return 'badge-gray';
    }
  };

  const getRoleDisplayName = (roleName) => {
    switch (roleName) {
      case 'admin':
        return 'Admin';
      case 'evm_staff':
        return 'EVM Staff';
      case 'dealer_manager':
        return 'Dealer Manager';
      case 'dealer_staff':
        return 'Dealer Staff';
      default:
        return roleName;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="user-detail-modal">
        <div className="modal-header">
          <div className="modal-title-container">
            <div className="user-avatar-large">
              {user.username?.charAt(0).toUpperCase()}
            </div>
            <div className="user-title-info">
              <h3 className="modal-title">{user.username}</h3>
              <p className="user-subtitle">
                {user.firstName && user.lastName 
                  ? `${user.firstName} ${user.lastName}`
                  : 'Chưa cập nhật thông tin'
                }
              </p>
            </div>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        
        <div className="modal-body">
          <div className="user-details-grid">
            <div className="detail-section">
              <h4>Thông tin cơ bản</h4>
              <div className="detail-items">
                <div className="detail-item">
                  <User className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Tên đăng nhập</span>
                    <span className="detail-value">{user.username || 'N/A'}</span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <Mail className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Email</span>
                    <span className="detail-value">{user.email || 'N/A'}</span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <Phone className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Số điện thoại</span>
                    <span className="detail-value">{user.phone || 'N/A'}</span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <MapPin className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Địa chỉ</span>
                    <span className="detail-value">{user.address || 'N/A'}</span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <Calendar className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Ngày sinh</span>
                    <span className="detail-value">
                      {user.dateOfBirth 
                        ? new Date(user.dateOfBirth).toLocaleDateString('vi-VN')
                        : 'N/A'
                      }
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="detail-section">
              <h4>Thông tin hệ thống</h4>
              <div className="detail-items">
                <div className="detail-item">
                  <Shield className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Vai trò</span>
                    <span className={`badge ${getRoleBadgeClass(user.role?.roleName || user.userType?.toLowerCase())}`}>
                      {getRoleDisplayName(user.role?.roleName || user.userType?.toLowerCase())}
                    </span>
                  </div>
                </div>
                
                {/* Hiển thị Đại lý nếu không phải admin */}
                {user.userType?.toUpperCase() !== 'ADMIN' && user.role?.roleName !== 'admin' && (
                  <div className="detail-item">
                    <MapPin className="detail-icon" />
                    <div className="detail-content">
                      <span className="detail-label">Đại lý</span>
                      <span className="detail-value">
                        {user.dealer?.dealerName 
                          ? `${user.dealer.dealerCode ? `${user.dealer.dealerCode}: ` : ''}${user.dealer.dealerName}`
                          : 'N/A'
                        }
                      </span>
                    </div>
                  </div>
                )}
                
                <div className="detail-item">
                  <div className="detail-icon">
                    <div className={`status-indicator ${user.isActive ? 'active' : 'inactive'}`}></div>
                  </div>
                  <div className="detail-content">
                    <span className="detail-label">Trạng thái</span>
                    <span className={`badge ${user.isActive ? 'badge-success' : 'badge-danger'}`}>
                      {user.isActive ? 'Hoạt động' : 'Không hoạt động'}
                    </span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <Clock className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Ngày tạo</span>
                    <span className="detail-value">{formatDate(user.createdAt)}</span>
                  </div>
                </div>
                
                <div className="detail-item">
                  <Clock className="detail-icon" />
                  <div className="detail-content">
                    <span className="detail-label">Cập nhật lần cuối</span>
                    <span className="detail-value">{formatDate(user.updatedAt)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserDetailModal;
