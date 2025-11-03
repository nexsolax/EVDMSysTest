import React, { useState, useEffect } from 'react';
import { X, Save, Building2, Calendar, Mail, Phone, MapPin, Hash, DollarSign } from 'lucide-react';
import { dealerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const DealerModal = ({ dealer, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    dealerName: '',
    dealerCode: '',
    dealerType: 'authorized',
    email: '',
    phone: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    licenseNumber: '',
    taxCode: '',
    commissionRate: 0,
    status: 'ACTIVE'
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setFormData({
          dealerName: '',
          dealerCode: '',
          dealerType: 'authorized',
          email: '',
          phone: '',
          address: '',
          city: '',
          province: '',
          postalCode: '',
          licenseNumber: '',
          taxCode: '',
          commissionRate: 0,
          status: 'ACTIVE'
        });
      } else if (dealer) {
        if (mode === 'edit' && dealer.dealerId) {
          // Load full details for edit mode
          loadDealerDetails();
        } else {
          // Use provided dealer data for view mode
          setFormData({
            dealerName: dealer.dealerName || '',
            dealerCode: dealer.dealerCode || '',
            dealerType: dealer.dealerType || 'authorized',
            email: dealer.email || '',
            phone: dealer.phone || '',
            address: dealer.address || '',
            city: dealer.city || '',
            province: dealer.province || '',
            postalCode: dealer.postalCode || '',
            licenseNumber: dealer.licenseNumber || '',
            taxCode: dealer.taxCode || '',
            commissionRate: dealer.commissionRate || 0,
            status: dealer.status || 'ACTIVE'
          });
        }
      }
    }
  }, [isOpen, dealer, mode]);

  const loadDealerDetails = async () => {
    if (!dealer?.dealerId) return;
    try {
      setLoading(true);
      const response = await dealerAPI.getDealer(dealer.dealerId);
      const dealerData = response.data;
      setFormData({
        dealerName: dealerData.dealerName || '',
        dealerCode: dealerData.dealerCode || '',
        dealerType: dealerData.dealerType || 'authorized',
        email: dealerData.email || '',
        phone: dealerData.phone || '',
        address: dealerData.address || '',
        city: dealerData.city || '',
        province: dealerData.province || '',
        postalCode: dealerData.postalCode || '',
        licenseNumber: dealerData.licenseNumber || '',
        taxCode: dealerData.taxCode || '',
        commissionRate: dealerData.commissionRate || 0,
        status: dealerData.status || 'ACTIVE'
      });
    } catch (error) {
      console.error('Error loading dealer details:', error);
      toast.error('Không thể tải chi tiết đại lý');
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
        dealerCode: formData.dealerCode.trim(),
        dealerName: formData.dealerName.trim(),
        email: formData.email.trim(),
        phone: formData.phone.trim() || null,
        address: formData.address.trim() || null,
        city: formData.city.trim() || null,
        province: formData.province.trim() || null,
        postalCode: formData.postalCode.trim() || null,
        dealerType: formData.dealerType || 'authorized',
        licenseNumber: formData.licenseNumber.trim() || null,
        taxCode: formData.taxCode.trim() || null,
        commissionRate: formData.commissionRate ? parseFloat(formData.commissionRate) : null,
        status: formData.status || 'ACTIVE'
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
                <option value="authorized">Đại lý ủy quyền</option>
                <option value="franchise">Đại lý nhượng quyền</option>
                <option value="partner">Đối tác</option>
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
              <label htmlFor="licenseNumber">Mã giấy phép</label>
              <input
                type="text"
                id="licenseNumber"
                name="licenseNumber"
                value={formData.licenseNumber}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập mã giấy phép"
              />
            </div>

            <div className="form-group">
              <label htmlFor="taxCode">Mã số thuế</label>
              <input
                type="text"
                id="taxCode"
                name="taxCode"
                value={formData.taxCode}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập mã số thuế"
              />
            </div>

            <div className="form-group">
              <label htmlFor="commissionRate">Tỷ lệ hoa hồng (%)</label>
              <input
                type="number"
                id="commissionRate"
                name="commissionRate"
                value={formData.commissionRate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập tỷ lệ hoa hồng"
                min="0"
                max="100"
                step="0.1"
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
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Không hoạt động</option>
                <option value="SUSPENDED">Tạm dừng</option>
                <option value="TERMINATED">Chấm dứt</option>
              </select>
            </div>

          </div>

          {mode === 'view' && dealer && (
            <div className="modal-info">
              <div className="info-item">
                <Building2 size={16} />
                <span><strong>Mã đại lý:</strong> {dealer.dealerCode || 'N/A'}</span>
              </div>
              <div className="info-item">
                <Mail size={16} />
                <span><strong>Email:</strong> {dealer.email || 'N/A'}</span>
              </div>
              {dealer.phone && (
                <div className="info-item">
                  <Phone size={16} />
                  <span><strong>Điện thoại:</strong> {dealer.phone}</span>
                </div>
              )}
              {dealer.address && (
                <div className="info-item">
                  <MapPin size={16} />
                  <span><strong>Địa chỉ:</strong> {dealer.address} {dealer.city ? `, ${dealer.city}` : ''} {dealer.province ? `, ${dealer.province}` : ''}</span>
                </div>
              )}
              {dealer.licenseNumber && (
                <div className="info-item">
                  <Hash size={16} />
                  <span><strong>Mã giấy phép:</strong> {dealer.licenseNumber}</span>
                </div>
              )}
              {dealer.taxCode && (
                <div className="info-item">
                  <Hash size={16} />
                  <span><strong>Mã số thuế:</strong> {dealer.taxCode}</span>
                </div>
              )}
              {dealer.commissionRate && (
                <div className="info-item">
                  <DollarSign size={16} />
                  <span><strong>Tỷ lệ hoa hồng:</strong> {dealer.commissionRate}%</span>
                </div>
              )}
              <div className="info-item">
                <Calendar size={16} />
                <span><strong>Ngày tạo:</strong> {dealer.createdAt ? new Date(dealer.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
              {dealer.updatedAt && (
                <div className="info-item">
                  <Calendar size={16} />
                  <span><strong>Ngày cập nhật:</strong> {new Date(dealer.updatedAt).toLocaleDateString('vi-VN')}</span>
                </div>
              )}
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
