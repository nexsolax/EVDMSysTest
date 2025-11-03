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
    const res = await apiFetch(`${baseUrl}/api/public/payments`, { method: 'POST', body: values });
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
