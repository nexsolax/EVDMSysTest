import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function QuotationForm({ baseUrl = '', onCreated }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    // Required fields (theo FIELD_REFERENCE_GUIDE.md line 537-551)
    const submitData = {};
    
    // Required fields
    if (values.variantId) {
      submitData.variantId = parseInt(values.variantId, 10);
    }
    if (values.colorId) {
      submitData.colorId = parseInt(values.colorId, 10);
    }
    if (values.totalPrice) {
      submitData.totalPrice = parseFloat(values.totalPrice);
    }
    if (values.finalPrice) {
      submitData.finalPrice = parseFloat(values.finalPrice);
    }
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (values.customerId) {
      submitData.customerId = values.customerId;
    }
    if (values.quotationDate?.trim()) {
      submitData.quotationDate = values.quotationDate.trim(); // Format: YYYY-MM-DD
    }
    if (values.discountAmount) {
      submitData.discountAmount = parseFloat(values.discountAmount);
    }
    if (values.validityDays) {
      submitData.validityDays = parseInt(values.validityDays, 10);
    }
    if (values.status?.trim()) {
      submitData.status = values.status.trim(); // lowercase
    }
    if (values.notes?.trim()) {
      submitData.notes = values.notes.trim();
    }
    
    const created = await apiFetch(`${baseUrl}/api/quotations`, { method: 'POST', body: submitData });
    onCreated?.(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Customer ID" {...register('customerId')} />
      {errors.customerId && <small>{errors.customerId.message}</small>}

      <input placeholder="Variant ID" {...register('variantId')} />
      <input placeholder="Base Price" type="number" step="0.01" {...register('basePrice', { valueAsNumber: true })} />
      {errors.basePrice && <small>{errors.basePrice.message}</small>}

      <input placeholder="Discount" type="number" step="0.01" {...register('discount', { valueAsNumber: true })} />
      <input placeholder="Tax" type="number" step="0.01" {...register('tax', { valueAsNumber: true })} />
      <input placeholder="Valid Until (YYYY-MM-DD)" {...register('validUntil')} />
      <textarea placeholder="Notes" {...register('notes')} />

      <button type="submit" disabled={isSubmitting}>Create Quotation</button>
    </form>
  );
}
