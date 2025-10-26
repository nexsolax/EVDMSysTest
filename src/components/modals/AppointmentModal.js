import React, { useState } from 'react';
import { X, Car, User, Mail, Phone, Calendar, Clock, MessageSquare } from 'lucide-react';
import { publicAppointmentAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const AppointmentModal = ({ isOpen, onClose, vehicle }) => {
  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    phone: '',
    appointmentDate: '',
    appointmentTime: '',
    appointmentType: 'test_drive',
    message: '',
    vehicleId: vehicle?.inventoryId || '',
    variantId: vehicle?.variantId || ''
  });

  const [loading, setLoading] = useState(false);

  const appointmentTypes = [
    { value: 'test_drive', label: 'Lái thử xe' },
    { value: 'consultation', label: 'Tư vấn mua xe' },
    { value: 'service', label: 'Dịch vụ bảo hành' },
    { value: 'other', label: 'Khác' }
  ];

  const timeSlots = [
    '08:00', '09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00'
  ];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.customerName || !formData.email || !formData.phone || !formData.appointmentDate || !formData.appointmentTime) {
      toast.error('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    try {
      setLoading(true);
      await publicAppointmentAPI.createAppointment(formData);
      toast.success('Đặt lịch hẹn thành công! Chúng tôi sẽ liên hệ lại với bạn.');
      onClose();
      setFormData({
        customerName: '',
        email: '',
        phone: '',
        appointmentDate: '',
        appointmentTime: '',
        appointmentType: 'test_drive',
        message: '',
        vehicleId: '',
        variantId: ''
      });
    } catch (error) {
      console.error('Error booking appointment:', error);
      toast.error('Có lỗi xảy ra khi đặt lịch hẹn');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content appointment-modal">
        <div className="modal-header">
          <h2>Đặt lịch hẹn</h2>
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

          <form onSubmit={handleSubmit} className="appointment-form">
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
              <label htmlFor="appointmentType">
                <Calendar className="label-icon" />
                Loại lịch hẹn *
              </label>
              <select
                id="appointmentType"
                name="appointmentType"
                value={formData.appointmentType}
                onChange={handleInputChange}
                required
              >
                {appointmentTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="appointmentDate">
                  <Calendar className="label-icon" />
                  Ngày hẹn *
                </label>
                <input
                  type="date"
                  id="appointmentDate"
                  name="appointmentDate"
                  value={formData.appointmentDate}
                  onChange={handleInputChange}
                  required
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>

              <div className="form-group">
                <label htmlFor="appointmentTime">
                  <Clock className="label-icon" />
                  Giờ hẹn *
                </label>
                <select
                  id="appointmentTime"
                  name="appointmentTime"
                  value={formData.appointmentTime}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Chọn giờ</option>
                  {timeSlots.map(time => (
                    <option key={time} value={time}>
                      {time}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="message">
                <MessageSquare className="label-icon" />
                Ghi chú bổ sung
              </label>
              <textarea
                id="message"
                name="message"
                value={formData.message}
                onChange={handleInputChange}
                rows={4}
                placeholder="Nhập ghi chú bổ sung (nếu có)"
              />
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Đang đặt lịch...' : 'Đặt lịch hẹn'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AppointmentModal;
