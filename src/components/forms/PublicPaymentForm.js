import React from 'react';
import { useForm } from 'react-hook-form';
import '../modals/Modal.css';
import './Forms.css';
import { apiFetch } from '../../utils/apiClient';

export default function PublicPaymentForm({ baseUrl = '', onPaid, defaultOrderId = '' }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { orderId: defaultOrderId, currency: 'USD', method: 'CARD' }
  });

  const onSubmit = async (values) => {
    // Required fields (theo INPUT_ORDER_GUIDE.md line 441-448 và 460-470)
    const submitData = {
      orderId: values.orderId // Required
    };
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (values.amount) {
      submitData.amount = parseFloat(values.amount);
    }
    if (values.paymentMethod?.trim()) {
      submitData.paymentMethod = values.paymentMethod.trim().toLowerCase(); // lowercase
    }
    if (values.notes?.trim()) {
      submitData.notes = values.notes.trim();
    }
    if (values.currency?.trim()) {
      submitData.currency = values.currency.trim();
    }
    if (values.cardLast4?.trim()) {
      submitData.cardLast4 = values.cardLast4.trim();
    }
    if (values.returnUrl?.trim()) {
      submitData.returnUrl = values.returnUrl.trim();
    }
    
    const res = await apiFetch(`${baseUrl}/api/public/payments`, { method: 'POST', body: submitData });
    onPaid?.(res);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Order ID" {...register('orderId')} />
      {errors.orderId && <small>{errors.orderId.message}</small>}

      <input placeholder="Amount" type="number" step="0.01" {...register('amount', { valueAsNumber: true })} />
      {errors.amount && <small>{errors.amount.message}</small>}

      <input placeholder="Currency (3 letters)" {...register('currency')} />
      <select {...register('method')}>
        <option value="CARD">CARD</option>
        <option value="BANK_TRANSFER">BANK_TRANSFER</option>
        <option value="CASH">CASH</option>
        <option value="WALLET">WALLET</option>
      </select>
      <input placeholder="Card last4" {...register('cardLast4')} />
      <input placeholder="Return URL" {...register('returnUrl')} />

      <button type="submit" disabled={isSubmitting}>Pay</button>
    </form>
  );
}
