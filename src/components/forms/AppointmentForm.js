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
    // Convert appointmentDate + appointmentTime to appointmentDate if needed
    if (values.appointmentDate && values.appointmentTime) {
      values.appointmentDate = `${values.appointmentDate}T${values.appointmentTime}:00`;
      delete values.appointmentTime;
    }
    
    // Remove preferredDateTime if appointmentDate is provided
    if (values.appointmentDate) {
      delete values.preferredDateTime;
    }
    
    if (onCreated) {
      await onCreated(values);
    } else {
      const created = await apiFetch(`${baseUrl}/api/appointments`, { method: 'POST', body: values });
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

