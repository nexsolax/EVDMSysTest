import React, { useState } from 'react';
import { X, Car } from 'lucide-react';
import { publicAppointmentAPI } from '../../services/api';
import AppointmentForm from '../forms/AppointmentForm';
import toast from 'react-hot-toast';
import './Modal.css';

const AppointmentModal = ({ isOpen, onClose, vehicle }) => {
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (formData) => {
    // If formData has appointmentDate and appointmentTime, combine them
    if (formData.appointmentDate && formData.appointmentTime) {
      const dateTime = `${formData.appointmentDate}T${formData.appointmentTime}:00`;
      formData.preferredDateTime = dateTime;
    }

    // Add vehicle info if available
    if (vehicle) {
      formData.inventoryId = vehicle.inventoryId || null;
      formData.variantId = vehicle.variantId || vehicle.variant?.variantId || null;
    }

    try {
      setLoading(true);
      await publicAppointmentAPI.createAppointment(formData);
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
