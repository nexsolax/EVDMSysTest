import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function QuotationForm({ baseUrl = '', onCreated }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    const created = await apiFetch(`${baseUrl}/api/quotations`, { method: 'POST', body: values });
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
