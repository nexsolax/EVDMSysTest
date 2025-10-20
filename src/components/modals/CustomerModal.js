import React, { useState, useEffect } from 'react';
import { X, Save, User, Calendar, Star } from 'lucide-react';
import { customerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const CustomerModal = ({ customer, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    postalCode: '',
    dateOfBirth: '',
    creditScore: 0,
    isActive: true
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && customer) {
      if (mode === 'edit') {
        loadCustomerDetails();
      } else {
        setFormData({
          firstName: customer.firstName || '',
          lastName: customer.lastName || '',
          email: customer.email || '',
          phone: customer.phone || '',
          address: customer.address || '',
          city: customer.city || '',
          postalCode: customer.postalCode || '',
          dateOfBirth: customer.dateOfBirth ? customer.dateOfBirth.split('T')[0] : '',
          creditScore: customer.creditScore || 0,
          isActive: customer.isActive || false
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, customer, mode]);

  const loadCustomerDetails = async () => {
    try {
      setLoading(true);
      const response = await customerAPI.getCustomer(customer.customerId);
      const customerData = response.data;
      setFormData({
        firstName: customerData.firstName || '',
        lastName: customerData.lastName || '',
        email: customerData.email || '',
        phone: customerData.phone || '',
        address: customerData.address || '',
        city: customerData.city || '',
        postalCode: customerData.postalCode || '',
        dateOfBirth: customerData.dateOfBirth ? customerData.dateOfBirth.split('T')[0] : '',
        creditScore: customerData.creditScore || 0,
        isActive: customerData.isActive || false
      });
    } catch (error) {
      console.error('Error loading customer details:', error);
      toast.error('Không thể tải thông tin khách hàng');
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
      await onSave(customer.customerId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving customer:', error);
    } finally {
      setLoading(false);
    }
  };

  const getCreditScoreColor = (score) => {
    if (score >= 750) return '#10b981'; // green
    if (score >= 650) return '#f59e0b'; // yellow
    return '#ef4444'; // red
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <User size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết khách hàng' : 'Chỉnh sửa khách hàng'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
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
                required
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
                required
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
                required
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
              <label htmlFor="address">Địa chỉ</label>
              <input
                type="text"
                id="address"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="city">Thành phố</label>
              <input
                type="text"
                id="city"
                name="city"
                value={formData.city}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="postalCode">Mã bưu điện</label>
              <input
                type="text"
                id="postalCode"
                name="postalCode"
                value={formData.postalCode}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="dateOfBirth">Ngày sinh</label>
              <input
                type="date"
                id="dateOfBirth"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="creditScore">Điểm tín dụng</label>
              <input
                type="number"
                id="creditScore"
                name="creditScore"
                value={formData.creditScore}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="300"
                max="850"
              />
              {mode === 'view' && (
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px', 
                  marginTop: '4px',
                  color: getCreditScoreColor(formData.creditScore)
                }}>
                  <Star size={16} />
                  <span style={{ fontSize: '12px' }}>
                    {formData.creditScore >= 750 ? 'Tốt' : 
                     formData.creditScore >= 650 ? 'Trung bình' : 'Kém'}
                  </span>
                </div>
              )}
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
                <span>Ngày tạo: {customer.createdAt ? new Date(customer.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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

export default CustomerModal;
