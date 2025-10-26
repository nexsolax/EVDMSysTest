import React, { useState } from 'react';
import { X, Car, User, Mail, Phone, MessageSquare, Calendar } from 'lucide-react';
import { publicQuotationAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const QuoteModal = ({ isOpen, onClose, vehicle }) => {
  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    phone: '',
    message: '',
    preferredDate: '',
    vehicleId: vehicle?.inventoryId || '',
    variantId: vehicle?.variantId || ''
  });

  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.customerName || !formData.email || !formData.phone) {
      toast.error('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    try {
      setLoading(true);
      await publicQuotationAPI.createQuotation(formData);
      toast.success('Yêu cầu báo giá đã được gửi thành công!');
      onClose();
      setFormData({
        customerName: '',
        email: '',
        phone: '',
        message: '',
        preferredDate: '',
        vehicleId: '',
        variantId: ''
      });
    } catch (error) {
      console.error('Error submitting quote request:', error);
      toast.error('Có lỗi xảy ra khi gửi yêu cầu báo giá');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content quote-modal">
        <div className="modal-header">
          <h2>Yêu cầu báo giá</h2>
          <button className="close-btn" onClick={onClose}>
            <X className="icon" />
          </button>
        </div>

        <div className="modal-body">
          {vehicle && (
            <div className="vehicle-info">
              <div className="vehicle-image">
                <Car className="icon" />
              </div>
              <div className="vehicle-details">
                <h3>{vehicle.variantName}</h3>
                <p>{vehicle.brand?.brandName} {vehicle.model?.modelName}</p>
                <p className="price">{vehicle.price?.toLocaleString('vi-VN')} VNĐ</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="quote-form">
            <div className="form-group">
              <label htmlFor="customerName">
                <User className="label-icon" />
                Họ và tên *
              </label>
              <input
                type="text"
                id="customerName"
                name="customerName"
                value={formData.customerName}
                onChange={handleInputChange}
                required
                placeholder="Nhập họ và tên của bạn"
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">
                <Mail className="label-icon" />
                Email *
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="Nhập địa chỉ email"
              />
            </div>

            <div className="form-group">
              <label htmlFor="phone">
                <Phone className="label-icon" />
                Số điện thoại *
              </label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                required
                placeholder="Nhập số điện thoại"
              />
            </div>

            <div className="form-group">
              <label htmlFor="preferredDate">
                <Calendar className="label-icon" />
                Ngày muốn nhận báo giá
              </label>
              <input
                type="date"
                id="preferredDate"
                name="preferredDate"
                value={formData.preferredDate}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label htmlFor="message">
                <MessageSquare className="label-icon" />
                Tin nhắn bổ sung
              </label>
              <textarea
                id="message"
                name="message"
                value={formData.message}
                onChange={handleInputChange}
                rows={4}
                placeholder="Nhập tin nhắn bổ sung (nếu có)"
              />
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Đang gửi...' : 'Gửi yêu cầu báo giá'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default QuoteModal;
