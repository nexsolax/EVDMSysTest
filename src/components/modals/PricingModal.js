import React, { useState, useEffect } from 'react';
import { X, Save, DollarSign, Calendar } from 'lucide-react';
import { dealerAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const PricingModal = ({ policy, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    policyName: '',
    policyType: 'DISCOUNT',
    scope: 'GLOBAL',
    dealerId: '',
    variantId: '',
    policyValue: 0,
    startDate: '',
    endDate: '',
    status: 'active',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [dealers, setDealers] = useState([]);
  const [variants, setVariants] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadDealers();
      loadVariants();
      
      if (mode === 'create') {
        setFormData({
          policyName: '',
          policyType: 'DISCOUNT',
          scope: 'GLOBAL',
          dealerId: '',
          variantId: '',
          policyValue: 0,
          startDate: '',
          endDate: '',
          status: 'active',
          description: ''
        });
      } else if (policy) {
        setFormData({
          policyName: policy.policyName || '',
          policyType: policy.policyType || 'DISCOUNT',
          scope: policy.scope || 'GLOBAL',
          dealerId: policy.dealer?.dealerId || '',
          variantId: policy.variant?.variantId || '',
          policyValue: policy.policyValue || 0,
          startDate: policy.startDate ? policy.startDate.split('T')[0] : '',
          endDate: policy.endDate ? policy.endDate.split('T')[0] : '',
          status: policy.status || 'active',
          description: policy.description || ''
        });
      }
    }
  }, [isOpen, policy, mode]);

  const loadDealers = async () => {
    try {
      const response = await dealerAPI.getDealers();
      setDealers(response.data || []);
    } catch (error) {
      console.error('Error loading dealers:', error);
    }
  };

  const loadVariants = async () => {
    try {
      const response = await vehicleAPI.getVariants();
      setVariants(response.data || []);
    } catch (error) {
      console.error('Error loading variants:', error);
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
    if (!formData.policyName.trim()) {
      toast.error('Tên chính sách là bắt buộc');
      return;
    }
    if (formData.policyValue <= 0) {
      toast.error('Giá trị chính sách phải lớn hơn 0');
      return;
    }
    if (formData.scope === 'DEALER' && !formData.dealerId) {
      toast.error('Vui lòng chọn đại lý');
      return;
    }
    if (formData.scope === 'VARIANT' && !formData.variantId) {
      toast.error('Vui lòng chọn dòng xe');
      return;
    }

    try {
      setLoading(true);
      
      // Required fields (theo FIELD_REFERENCE_GUIDE.md line 633-655)
      const submitData = {
        policyName: formData.policyName.trim() // Required
      };
      
      // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
      if (formData.variantId) {
        submitData.variantId = parseInt(formData.variantId, 10);
      }
      if (formData.dealerId && formData.scope === 'DEALER') {
        submitData.dealerId = formData.dealerId;
      }
      if (formData.policyType?.trim()) {
        submitData.policyType = formData.policyType.trim();
      }
      if (formData.scope?.trim()) {
        submitData.scope = formData.scope.trim();
      }
      if (formData.basePrice) {
        submitData.basePrice = parseFloat(formData.basePrice);
      }
      if (formData.discountPercent) {
        submitData.discountPercent = parseFloat(formData.discountPercent);
      }
      if (formData.discountAmount) {
        submitData.discountAmount = parseFloat(formData.discountAmount);
      }
      if (formData.markupPercent) {
        submitData.markupPercent = parseFloat(formData.markupPercent);
      }
      if (formData.markupAmount) {
        submitData.markupAmount = parseFloat(formData.markupAmount);
      }
      if (formData.startDate?.trim() || formData.effectiveDate?.trim()) {
        submitData.effectiveDate = (formData.startDate || formData.effectiveDate).trim(); // Format: YYYY-MM-DD
      }
      if (formData.endDate?.trim() || formData.expiryDate?.trim()) {
        submitData.expiryDate = (formData.endDate || formData.expiryDate).trim(); // Format: YYYY-MM-DD
      }
      if (formData.minQuantity) {
        submitData.minQuantity = parseInt(formData.minQuantity, 10);
      }
      if (formData.maxQuantity) {
        submitData.maxQuantity = parseInt(formData.maxQuantity, 10);
      }
      if (formData.customerType?.trim()) {
        submitData.customerType = formData.customerType.trim();
      }
      if (formData.region?.trim()) {
        submitData.region = formData.region.trim();
      }
      if (formData.status?.trim()) {
        submitData.status = formData.status.trim(); // lowercase
      }
      if (formData.priority !== undefined && formData.priority !== null) {
        submitData.priority = parseInt(formData.priority, 10);
      }
      if (formData.description?.trim()) {
        submitData.description = formData.description.trim();
      }

      console.log('Sending pricing policy data:', submitData);
      
      if (mode === 'create') {
        await onSave(null, submitData);
      } else {
        await onSave(policy.policyId, submitData);
      }
    } catch (error) {
      console.error('Error saving pricing policy:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || error.message;
        toast.error(`Lỗi: ${errorMessage}`);
      } else {
        toast.error(mode === 'create' ? 'Không thể tạo chính sách giá' : 'Không thể cập nhật chính sách giá');
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
            <DollarSign size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết chính sách giá' : 
               mode === 'create' ? 'Thêm chính sách giá mới' : 
               'Chỉnh sửa chính sách giá'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="policyName">Tên chính sách *</label>
              <input
                type="text"
                id="policyName"
                name="policyName"
                value={formData.policyName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập tên chính sách"
              />
            </div>

            <div className="form-group">
              <label htmlFor="policyType">Loại chính sách</label>
              <select
                id="policyType"
                name="policyType"
                value={formData.policyType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="DISCOUNT">Giảm giá</option>
                <option value="SURCHARGE">Phụ phí</option>
                <option value="FIXED_PRICE">Giá cố định</option>
                <option value="PERCENTAGE">Phần trăm</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="scope">Phạm vi áp dụng</label>
              <select
                id="scope"
                name="scope"
                value={formData.scope}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="GLOBAL">Toàn hệ thống</option>
                <option value="DEALER">Theo đại lý</option>
                <option value="VARIANT">Theo dòng xe</option>
              </select>
            </div>

            {formData.scope === 'DEALER' && (
              <div className="form-group">
                <label htmlFor="dealerId">Đại lý *</label>
                <select
                  id="dealerId"
                  name="dealerId"
                  value={formData.dealerId}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-select"
                >
                  <option value="">Chọn đại lý</option>
                  {dealers.map((dealer) => (
                    <option key={dealer.dealerId} value={dealer.dealerId}>
                      {dealer.dealerName}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {formData.scope === 'VARIANT' && (
              <div className="form-group">
                <label htmlFor="variantId">Dòng xe *</label>
                <select
                  id="variantId"
                  name="variantId"
                  value={formData.variantId}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-select"
                >
                  <option value="">Chọn dòng xe</option>
                  {variants.map((variant) => (
                    <option key={variant.variantId} value={variant.variantId}>
                      {variant.variantName}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="form-group">
              <label htmlFor="policyValue">Giá trị chính sách *</label>
              <input
                type="number"
                id="policyValue"
                name="policyValue"
                value={formData.policyValue}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập giá trị"
                min="0"
                step="0.01"
              />
            </div>

            <div className="form-group">
              <label htmlFor="startDate">Ngày bắt đầu</label>
              <input
                type="date"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="endDate">Ngày kết thúc</label>
              <input
                type="date"
                id="endDate"
                name="endDate"
                value={formData.endDate}
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
                <option value="expired">Hết hạn</option>
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
                placeholder="Nhập mô tả về chính sách giá"
                rows="3"
              />
            </div>
          </div>

          {mode === 'view' && policy && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {policy.createdAt ? new Date(policy.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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
                {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo chính sách' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default PricingModal;
