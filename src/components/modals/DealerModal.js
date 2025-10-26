import React, { useState, useEffect } from 'react';
import { X, Save, Building2, Calendar } from 'lucide-react';
import { dealerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const DealerModal = ({ dealer, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    dealerName: '',
    dealerCode: '',
    dealerType: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    phone: '',
    email: '',
    website: '',
    establishedDate: '',
    status: 'active',
    description: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setFormData({
          dealerName: '',
          dealerCode: '',
          dealerType: 'AUTHORIZED',
          address: '',
          city: '',
          province: '',
          postalCode: '',
          phone: '',
          email: '',
          website: '',
          establishedDate: '',
          status: 'active',
          description: ''
        });
      } else if (dealer) {
        setFormData({
          dealerName: dealer.dealerName || '',
          dealerCode: dealer.dealerCode || '',
          dealerType: dealer.dealerType || 'AUTHORIZED',
          address: dealer.address || '',
          city: dealer.city || '',
          province: dealer.province || '',
          postalCode: dealer.postalCode || '',
          phone: dealer.phone || '',
          email: dealer.email || '',
          website: dealer.website || '',
          establishedDate: dealer.establishedDate ? dealer.establishedDate.split('T')[0] : '',
          status: dealer.status || 'active',
          description: dealer.description || ''
        });
      }
    }
  }, [isOpen, dealer, mode]);

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

    // Validation
    if (!formData.dealerName.trim()) {
      toast.error('Tên đại lý là bắt buộc');
      return;
    }
    if (!formData.dealerCode.trim()) {
      toast.error('Mã đại lý là bắt buộc');
      return;
    }
    if (!formData.email.trim()) {
      toast.error('Email là bắt buộc');
      return;
    }

    try {
      setLoading(true);
      
      const submitData = {
        dealerName: formData.dealerName.trim(),
        dealerCode: formData.dealerCode.trim(),
        dealerType: formData.dealerType,
        address: formData.address.trim(),
        city: formData.city.trim(),
        province: formData.province.trim(),
        postalCode: formData.postalCode.trim(),
        phone: formData.phone.trim(),
        email: formData.email.trim(),
        website: formData.website.trim(),
        establishedDate: formData.establishedDate || null,
        status: formData.status,
        description: formData.description.trim()
      };

      console.log('Sending dealer data:', submitData);
      
      if (mode === 'create') {
        await onSave(null, submitData);
      } else {
        await onSave(dealer.dealerId, submitData);
      }
    } catch (error) {
      console.error('Error saving dealer:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || error.message;
        if (errorMessage.includes('dealerCode') && errorMessage.includes('already exists')) {
          toast.error('Mã đại lý đã tồn tại. Vui lòng chọn mã khác.');
        } else if (errorMessage.includes('email') && errorMessage.includes('already exists')) {
          toast.error('Email đã tồn tại. Vui lòng sử dụng email khác.');
        } else {
          toast.error(`Lỗi: ${errorMessage}`);
        }
      } else {
        toast.error(mode === 'create' ? 'Không thể tạo đại lý' : 'Không thể cập nhật đại lý');
      }
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
            <Building2 size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết đại lý' : 
               mode === 'create' ? 'Thêm đại lý mới' : 
               'Chỉnh sửa đại lý'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="dealerName">Tên đại lý *</label>
              <input
                type="text"
                id="dealerName"
                name="dealerName"
                value={formData.dealerName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập tên đại lý"
              />
            </div>

            <div className="form-group">
              <label htmlFor="dealerCode">Mã đại lý *</label>
              <input
                type="text"
                id="dealerCode"
                name="dealerCode"
                value={formData.dealerCode}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập mã đại lý"
              />
            </div>

            <div className="form-group">
              <label htmlFor="dealerType">Loại đại lý</label>
              <select
                id="dealerType"
                name="dealerType"
                value={formData.dealerType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="AUTHORIZED">Đại lý ủy quyền</option>
                <option value="FRANCHISE">Đại lý nhượng quyền</option>
                <option value="PARTNER">Đối tác</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="email">Email *</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập email"
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
                placeholder="Nhập số điện thoại"
              />
            </div>

            <div className="form-group">
              <label htmlFor="website">Website</label>
              <input
                type="url"
                id="website"
                name="website"
                value={formData.website}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="https://example.com"
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
                placeholder="Nhập địa chỉ"
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
                placeholder="Nhập thành phố"
              />
            </div>

            <div className="form-group">
              <label htmlFor="province">Tỉnh</label>
              <input
                type="text"
                id="province"
                name="province"
                value={formData.province}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập tỉnh"
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
                placeholder="Nhập mã bưu điện"
              />
            </div>

            <div className="form-group">
              <label htmlFor="establishedDate">Ngày thành lập</label>
              <input
                type="date"
                id="establishedDate"
                name="establishedDate"
                value={formData.establishedDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="active">Hoạt động</option>
                <option value="inactive">Không hoạt động</option>
                <option value="suspended">Tạm dừng</option>
                <option value="terminated">Chấm dứt</option>
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="description">Mô tả</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-textarea"
                placeholder="Nhập mô tả về đại lý"
                rows="3"
              />
            </div>
          </div>

          {mode === 'view' && dealer && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {dealer.createdAt ? new Date(dealer.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {(mode === 'edit' || mode === 'create') && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo đại lý' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default DealerModal;
