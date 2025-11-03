import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function AppointmentForm({ baseUrl = '', onCreated, defaultVariantId = null, defaultInventoryId = null, onCancel }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      variantId: defaultVariantId,
      inventoryId: defaultInventoryId
    }
  });

  const onSubmit = async (values) => {
    if (onCreated) {
      await onCreated(values);
    } else {
      const created = await apiFetch(`${baseUrl}/api/appointments`, { method: 'POST', body: values });
      onCreated?.(created);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Tên khách hàng *" {...register('customerName')} />
      {errors.customerName && <small>{errors.customerName.message}</small>}

      <input placeholder="Email" type="email" {...register('email')} />
      {errors.email && <small>{errors.email.message}</small>}

      <input placeholder="Số điện thoại" {...register('phone')} />
      {errors.phone && <small>{errors.phone.message}</small>}

      <input placeholder="Variant ID" type="number" {...register('variantId', { valueAsNumber: true })} />
      <input placeholder="Inventory ID (UUID)" {...register('inventoryId')} />
      
      <div style={{ display: 'flex', gap: '10px' }}>
        <input 
          placeholder="Ngày hẹn *" 
          type="date" 
          {...register('appointmentDate')} 
          min={new Date().toISOString().split('T')[0]}
        />
        <input 
          placeholder="Giờ hẹn *" 
          type="time" 
          {...register('appointmentTime')} 
        />
      </div>
      <input placeholder="Hoặc ngày giờ hẹn (YYYY-MM-DDTHH:mm)" type="datetime-local" {...register('preferredDateTime')} />
      {errors.preferredDateTime && <small>{errors.preferredDateTime.message}</small>}

      <textarea placeholder="Ghi chú" {...register('notes')} />
      {errors.notes && <small>{errors.notes.message}</small>}

      <div className="form-actions">
        {onCancel && (
          <button type="button" className="btn btn-secondary" onClick={onCancel}>
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

