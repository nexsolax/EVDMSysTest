import React, { useState, useEffect } from 'react';
import { X, Save, FileText, DollarSign, Calendar } from 'lucide-react';
import { quotationAPI, customerAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const QuotationModal = ({ quotation, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    vehicleId: '',
    quotationDate: '',
    validUntil: '',
    basePrice: '',
    discountAmount: '',
    taxAmount: '',
    totalAmount: '',
    status: 'DRAFT',
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
            vehicleId: quotation.vehicle?.vehicleId || quotation.vehicleId || '',
            quotationDate: quotation.quotationDate || '',
            validUntil: quotation.validUntil || '',
            basePrice: quotation.basePrice || '',
            discountAmount: quotation.discountAmount || '',
            taxAmount: quotation.taxAmount || '',
            totalAmount: quotation.totalAmount || '',
            status: quotation.status || 'DRAFT',
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
      const response = await vehicleAPI.getActiveVehicles();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadQuotationDetails = async () => {
    try {
      setLoading(true);
      const response = await quotationAPI.getQuotation(quotation.quotationId);
      const quotationData = response.data;
      setFormData({
        customerId: quotationData.customer?.customerId || quotationData.customerId || '',
        vehicleId: quotationData.vehicle?.vehicleId || quotationData.vehicleId || '',
        quotationDate: quotationData.quotationDate || '',
        validUntil: quotationData.validUntil || '',
        basePrice: quotationData.basePrice || '',
        discountAmount: quotationData.discountAmount || '',
        taxAmount: quotationData.taxAmount || '',
        totalAmount: quotationData.totalAmount || '',
        status: quotationData.status || 'DRAFT',
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

    // Auto calculate total amount
    if (name === 'basePrice' || name === 'discountAmount' || name === 'taxAmount') {
      const basePrice = parseFloat(name === 'basePrice' ? value : formData.basePrice) || 0;
      const discountAmount = parseFloat(name === 'discountAmount' ? value : formData.discountAmount) || 0;
      const taxAmount = parseFloat(name === 'taxAmount' ? value : formData.taxAmount) || 0;
      const totalAmount = basePrice - discountAmount + taxAmount;
      setFormData(prev => ({
        ...prev,
        totalAmount: totalAmount.toString()
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
              <label htmlFor="vehicleId">Xe</label>
              <select
                id="vehicleId"
                name="vehicleId"
                value={formData.vehicleId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn xe</option>
                {vehicles.map(vehicle => (
                  <option key={vehicle.vehicleId} value={vehicle.vehicleId}>
                    {vehicle.brand?.brandName} {vehicle.model?.modelName} {vehicle.variant?.variantName}
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
              <label htmlFor="validUntil">Có hiệu lực đến</label>
              <input
                type="date"
                id="validUntil"
                name="validUntil"
                value={formData.validUntil}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="basePrice">Giá cơ bản (VNĐ)</label>
              <input
                type="number"
                id="basePrice"
                name="basePrice"
                value={formData.basePrice}
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
              <label htmlFor="taxAmount">Thuế (VNĐ)</label>
              <input
                type="number"
                id="taxAmount"
                name="taxAmount"
                value={formData.taxAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="totalAmount">Tổng tiền (VNĐ)</label>
              <input
                type="number"
                id="totalAmount"
                name="totalAmount"
                value={formData.totalAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
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
                required
              >
                <option value="DRAFT">Nháp</option>
                <option value="SENT">Đã gửi</option>
                <option value="ACCEPTED">Đã chấp nhận</option>
                <option value="REJECTED">Đã từ chối</option>
                <option value="EXPIRED">Hết hạn</option>
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
                <span>Tổng tiền: {formData.totalAmount ? new Intl.NumberFormat('vi-VN').format(formData.totalAmount) + ' VNĐ' : 'N/A'}</span>
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

export default QuotationModal;
