import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function OrderForm({ baseUrl = '', onCreated }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    // Tất cả fields trong OrderRequest đều optional (theo FIELD_REFERENCE_GUIDE.md line 192-213)
    const submitData = {};
    
    // Chỉ thêm fields nếu có giá trị (không gửi null/undefined/empty)
    if (values.quotationId) {
      submitData.quotationId = values.quotationId;
    }
    if (values.customerId) {
      submitData.customerId = values.customerId;
    }
    if (values.userId) {
      submitData.userId = values.userId;
    }
    if (values.inventoryId) {
      submitData.inventoryId = values.inventoryId;
    }
    if (values.orderDate?.trim()) {
      submitData.orderDate = values.orderDate.trim(); // Format: YYYY-MM-DD
    }
    if (values.orderNumber?.trim()) {
      submitData.orderNumber = values.orderNumber.trim();
    }
    
    const created = await apiFetch(`${baseUrl}/api/orders`, { method: 'POST', body: submitData });
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
