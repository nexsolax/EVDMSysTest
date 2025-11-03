import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function OrderForm({ baseUrl = '', onCreated }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    const created = await apiFetch(`${baseUrl}/api/orders`, { method: 'POST', body: values });
    onCreated?.(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Order Number" {...register('orderNumber')} />
      {errors.orderNumber && <small>{errors.orderNumber.message}</small>}

      <input placeholder="Quotation ID" {...register('quotationId')} />
      <input placeholder="Customer ID" {...register('customerId')} />
      <input placeholder="User ID" {...register('userId')} />
      <input placeholder="Inventory ID" {...register('inventoryId')} />
      <input placeholder="Order Date (YYYY-MM-DD)" {...register('orderDate')} />

      <button type="submit" disabled={isSubmitting}>Create Order</button>
    </form>
  );
}
