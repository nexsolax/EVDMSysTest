import React, { useState, useEffect } from 'react';
import { X, Save, User, Calendar } from 'lucide-react';
import { userAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const UserModal = ({ user, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    role: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    if (isOpen && user) {
      if (mode === 'edit') {
        loadUserDetails();
      } else {
        setFormData({
          username: user.username || '',
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          email: user.email || '',
          phone: user.phone || '',
          role: user.role?.roleName || '',
          isActive: user.isActive || false
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, user, mode]);

  const loadUserDetails = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getUser(user.userId);
      const userData = response.data;
      setFormData({
        username: userData.username || '',
        firstName: userData.firstName || '',
        lastName: userData.lastName || '',
        email: userData.email || '',
        phone: userData.phone || '',
        role: userData.role?.roleName || '',
        isActive: userData.isActive || false
      });
    } catch (error) {
      console.error('Error loading user details:', error);
      toast.error('Không thể tải thông tin người dùng');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(user.userId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving user:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <User size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết người dùng' : 'Chỉnh sửa người dùng'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="username">Tên đăng nhập</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                disabled={mode === 'view' || true} // Username không thể sửa
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="firstName">Họ</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="lastName">Tên</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="phone">Số điện thoại</label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="role">Vai trò</label>
              <select
                id="role"
                name="role"
                value={formData.role}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="">Chọn vai trò</option>
                <option value="admin">Admin</option>
                <option value="evm_staff">EVM Staff</option>
                <option value="dealer_manager">Dealer Manager</option>
                <option value="dealer_staff">Dealer Staff</option>
              </select>
            </div>

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-checkbox"
                />
                <span>Đang hoạt động</span>
              </label>
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {mode === 'edit' && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserModal;
