import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function AppointmentForm({ baseUrl = '', onCreated, defaultVariantId = null, defaultInventoryId = null, defaultCustomerId = null, onCancel }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      customerId: defaultCustomerId,
      variantId: defaultVariantId,
      inventoryId: defaultInventoryId
    }
  });

  const onSubmit = async (values) => {
    // Required fields (theo FIELD_REFERENCE_GUIDE.md line 584-597)
    const submitData = {
      customerId: values.customerId,
      title: values.title || 'Lịch hẹn' // Required
    };
    
    // Convert appointmentDate + appointmentTime to appointmentDate if needed
    // Required - LocalDateTime format: YYYY-MM-DDTHH:mm:ss
    if (values.appointmentDate && values.appointmentTime) {
      submitData.appointmentDate = `${values.appointmentDate}T${values.appointmentTime}:00`;
    } else if (values.preferredDateTime) {
      submitData.appointmentDate = values.preferredDateTime;
    } else if (values.appointmentDate) {
      submitData.appointmentDate = `${values.appointmentDate}T10:00:00`; // Default time
    } else {
      // Fallback: use current date/time
      const now = new Date();
      submitData.appointmentDate = now.toISOString().slice(0, 19);
    }
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (values.variantId) {
      submitData.variantId = parseInt(values.variantId, 10);
    }
    if (values.appointmentType?.trim()) {
      submitData.appointmentType = values.appointmentType.trim();
    }
    if (values.description?.trim()) {
      submitData.description = values.description.trim();
    }
    if (values.durationMinutes) {
      submitData.durationMinutes = parseInt(values.durationMinutes, 10);
    }
    if (values.location?.trim()) {
      submitData.location = values.location.trim();
    }
    if (values.status?.trim()) {
      submitData.status = values.status.trim(); // lowercase
    }
    if (values.notes?.trim()) {
      submitData.notes = values.notes.trim();
    }
    
    if (onCreated) {
      await onCreated(submitData);
    } else {
      const created = await apiFetch(`${baseUrl}/api/appointments`, { method: 'POST', body: submitData });
      onCreated?.(created);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="customerId">ID Khách hàng *</label>
          <input 
            id="customerId"
            type="text" 
            placeholder="UUID khách hàng" 
            {...register('customerId', { required: 'ID khách hàng là bắt buộc' })} 
          />
          {errors.customerId && <small className="error">{errors.customerId.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="variantId">Variant ID</label>
          <input 
            id="variantId"
            type="number" 
            placeholder="ID phiên bản xe" 
            {...register('variantId', { valueAsNumber: true })} 
          />
          {errors.variantId && <small className="error">{errors.variantId.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="inventoryId">Inventory ID</label>
          <input 
            id="inventoryId"
            type="text" 
            placeholder="UUID tồn kho" 
            {...register('inventoryId')} 
          />
          {errors.inventoryId && <small className="error">{errors.inventoryId.message}</small>}
        </div>
        
        <div className="form-group">
          <label htmlFor="appointmentDate">Ngày hẹn *</label>
          <input 
            id="appointmentDate"
            type="date" 
            {...register('appointmentDate', { required: 'Ngày hẹn là bắt buộc' })} 
            min={new Date().toISOString().split('T')[0]}
          />
          {errors.appointmentDate && <small className="error">{errors.appointmentDate.message}</small>}
        </div>
        
        <div className="form-group">
          <label htmlFor="appointmentTime">Giờ hẹn</label>
          <input 
            id="appointmentTime"
            type="time" 
            {...register('appointmentTime')} 
          />
          {errors.appointmentTime && <small className="error">{errors.appointmentTime.message}</small>}
        </div>
        
        <div className="form-group full-width">
          <label htmlFor="preferredDateTime">Hoặc ngày giờ hẹn (YYYY-MM-DDTHH:mm)</label>
          <input 
            id="preferredDateTime"
            type="datetime-local" 
            {...register('preferredDateTime')} 
          />
          {errors.preferredDateTime && <small className="error">{errors.preferredDateTime.message}</small>}
        </div>

        <div className="form-group full-width">
          <label htmlFor="notes">Ghi chú</label>
          <textarea 
            id="notes"
            placeholder="Ghi chú" 
            {...register('notes')} 
            className="form-textarea"
            rows="3"
          />
          {errors.notes && <small className="error">{errors.notes.message}</small>}
        </div>
      </div>

      <div className="modal-actions">
        {onCancel && (
          <button type="button" className="btn btn-outline" onClick={onCancel}>
            Hủy
          </button>
        )}
        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Đang đặt lịch...' : 'Đặt lịch hẹn'}
        </button>
      </div>
    </form>
  );
}

