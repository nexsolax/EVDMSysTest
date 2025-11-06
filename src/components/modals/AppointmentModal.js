import React, { useState } from 'react';
import { X, Car } from 'lucide-react';
import { publicAppointmentAPI } from '../../services/api';
import AppointmentForm from '../forms/AppointmentForm';
import toast from 'react-hot-toast';
import './Modal.css';

const AppointmentModal = ({ isOpen, onClose, vehicle }) => {
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (formData) => {
    // Required fields (theo FIELD_REFERENCE_GUIDE.md line 584-597)
    const submitData = {
      title: formData.title?.trim() || 'Lịch hẹn',
      appointmentDate: formData.appointmentDate // Required - LocalDateTime format: YYYY-MM-DDTHH:mm:ss
    };
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (formData.customerId) {
      submitData.customerId = formData.customerId;
    }
    if (vehicle?.inventoryId || formData.inventoryId) {
      submitData.inventoryId = vehicle?.inventoryId || formData.inventoryId;
    }
    if (vehicle?.variantId || vehicle?.variant?.variantId || formData.variantId) {
      submitData.variantId = parseInt(vehicle?.variantId || vehicle?.variant?.variantId || formData.variantId, 10);
    }
    if (formData.appointmentType?.trim()) {
      submitData.appointmentType = formData.appointmentType.trim();
    }
    if (formData.description?.trim()) {
      submitData.description = formData.description.trim();
    }
    if (formData.durationMinutes) {
      submitData.durationMinutes = parseInt(formData.durationMinutes, 10);
    }
    if (formData.location?.trim()) {
      submitData.location = formData.location.trim();
    }
    if (formData.status?.trim()) {
      submitData.status = formData.status.trim(); // lowercase
    }
    if (formData.notes?.trim()) {
      submitData.notes = formData.notes.trim();
    }

    try {
      setLoading(true);
      await publicAppointmentAPI.createAppointment(submitData);
      toast.success('Đặt lịch hẹn thành công! Chúng tôi sẽ liên hệ lại với bạn.');
      onClose();
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
                <h3>{vehicle.variantName || vehicle.variant?.variantName}</h3>
                <p>{vehicle.brand?.brandName || vehicle.variant?.brand?.brandName} {vehicle.model?.modelName || vehicle.variant?.model?.modelName}</p>
                <p className="price">{(vehicle.variant?.priceBase || vehicle.priceBase || vehicle.price)?.toLocaleString('vi-VN')} VNĐ</p>
              </div>
            </div>
          )}

          <div className="appointment-form-wrapper">
            <AppointmentForm
              baseUrl=""
              onCreated={handleSubmit}
              onCancel={onClose}
              defaultVariantId={vehicle?.variantId || vehicle?.variant?.variantId}
              defaultInventoryId={vehicle?.inventoryId}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default AppointmentModal;
