import React, { useState, useEffect } from 'react';
import { X, Save, FileText, DollarSign, Calendar } from 'lucide-react';
import { quotationAPI, customerAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const QuotationModal = ({ quotation, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    variantId: '',
    quotationDate: '',
    totalPrice: '',
    discountAmount: '',
    finalPrice: '',
    validityDays: 7,
    status: 'pending',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadCustomers();
      loadVehicles();
      if (quotation) {
        if (mode === 'edit') {
          loadQuotationDetails();
        } else {
          setFormData({
            customerId: quotation.customer?.customerId || quotation.customerId || '',
            variantId: quotation.variant?.variantId || quotation.variantId || '',
            quotationDate: quotation.quotationDate || new Date().toISOString().split('T')[0],
            totalPrice: quotation.totalPrice || quotation.basePrice || '',
            discountAmount: quotation.discountAmount || 0,
            finalPrice: quotation.finalPrice || quotation.totalPrice || '',
            validityDays: quotation.validityDays || 7,
            status: quotation.status || 'pending',
            notes: quotation.notes || ''
          });
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, quotation, mode]);

  const loadCustomers = async () => {
    try {
      const response = await customerAPI.getActiveCustomers();
      setCustomers(response.data || []);
    } catch (error) {
      console.error('Error loading customers:', error);
    }
  };

  const loadVehicles = async () => {
    try {
      // Load variants instead of vehicles
      const response = await vehicleAPI.getVariants();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading variants:', error);
    }
  };

  const loadQuotationDetails = async () => {
    try {
      setLoading(true);
      const response = await quotationAPI.getQuotation(quotation.quotationId);
      const quotationData = response.data;
      setFormData({
        customerId: quotationData.customer?.customerId || quotationData.customerId || '',
        variantId: quotationData.variant?.variantId || quotationData.variantId || '',
        quotationDate: quotationData.quotationDate || new Date().toISOString().split('T')[0],
        totalPrice: quotationData.totalPrice || quotationData.basePrice || '',
        discountAmount: quotationData.discountAmount || 0,
        finalPrice: quotationData.finalPrice || quotationData.totalPrice || '',
        validityDays: quotationData.validityDays || 7,
        status: quotationData.status || 'pending',
        notes: quotationData.notes || ''
      });
    } catch (error) {
      console.error('Error loading quotation details:', error);
      toast.error('Không thể tải thông tin báo giá');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Auto calculate final price
    if (name === 'totalPrice' || name === 'discountAmount') {
      const totalPrice = parseFloat(name === 'totalPrice' ? value : formData.totalPrice) || 0;
      const discountAmount = parseFloat(name === 'discountAmount' ? value : formData.discountAmount) || 0;
      const finalPrice = totalPrice - discountAmount;
      setFormData(prev => ({
        ...prev,
        finalPrice: finalPrice.toString()
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(quotation.quotationId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving quotation:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container large">
        <div className="modal-header">
          <div className="modal-title">
            <FileText size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết báo giá' : 'Chỉnh sửa báo giá'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="customerId">Khách hàng</label>
              <select
                id="customerId"
                name="customerId"
                value={formData.customerId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn khách hàng</option>
                {customers.map(customer => (
                  <option key={customer.customerId} value={customer.customerId}>
                    {customer.firstName} {customer.lastName} - {customer.email}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="variantId">Phiên bản xe</label>
              <select
                id="variantId"
                name="variantId"
                value={formData.variantId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn phiên bản xe</option>
                {vehicles.map(variant => (
                  <option key={variant.variantId} value={variant.variantId}>
                    {variant.model?.brand?.brandName} {variant.model?.modelName} {variant.variantName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="quotationDate">Ngày báo giá</label>
              <input
                type="date"
                id="quotationDate"
                name="quotationDate"
                value={formData.quotationDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="totalPrice">Giá gốc (VNĐ) *</label>
              <input
                type="number"
                id="totalPrice"
                name="totalPrice"
                value={formData.totalPrice}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="discountAmount">Giảm giá (VNĐ)</label>
              <input
                type="number"
                id="discountAmount"
                name="discountAmount"
                value={formData.discountAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="finalPrice">Giá cuối cùng (VNĐ) *</label>
              <input
                type="number"
                id="finalPrice"
                name="finalPrice"
                value={formData.finalPrice}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
              <small className="form-help">Giá cuối cùng = Giá gốc - Giảm giá</small>
            </div>

            <div className="form-group">
              <label htmlFor="validityDays">Số ngày hiệu lực</label>
              <input
                type="number"
                id="validityDays"
                name="validityDays"
                value={formData.validityDays}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="1"
                max="365"
                required
              />
              <small className="form-help">Mặc định: 7 ngày</small>
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
                required
              >
                <option value="pending">Chờ phản hồi</option>
                <option value="accepted">Đã chấp nhận</option>
                <option value="rejected">Đã từ chối</option>
                <option value="expired">Hết hạn</option>
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="notes">Ghi chú</label>
              <textarea
                id="notes"
                name="notes"
                value={formData.notes}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                rows={4}
                placeholder="Ghi chú về báo giá..."
              />
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Giá gốc: {formData.totalPrice ? new Intl.NumberFormat('vi-VN').format(formData.totalPrice) + ' VNĐ' : 'N/A'}</span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Giá cuối cùng: {formData.finalPrice ? new Intl.NumberFormat('vi-VN').format(formData.finalPrice) + ' VNĐ' : 'N/A'}</span>
              </div>
              {formData.validityDays && (
                <div className="info-item">
                  <Calendar size={16} />
                  <span>Hiệu lực: {formData.validityDays} ngày</span>
                </div>
              )}
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

export default QuotationModal;
