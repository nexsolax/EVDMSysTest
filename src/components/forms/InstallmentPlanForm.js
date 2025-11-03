import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function InstallmentPlanForm({ baseUrl = '', onCreated, defaultOrderId = null, defaultCustomerId = null }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      orderId: defaultOrderId,
      customerId: defaultCustomerId,
      planType: 'monthly',
      status: 'active'
    }
  });

  const onSubmit = async (values) => {
    const created = await apiFetch(`${baseUrl}/api/installment-plans`, { method: 'POST', body: values });
    onCreated?.(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Số hợp đồng *" {...register('contractNumber', { required: 'Số hợp đồng là bắt buộc' })} />
      {errors.contractNumber && <small>{errors.contractNumber.message}</small>}

      <input placeholder="Order ID (UUID)" {...register('orderId')} />
      <input placeholder="Customer ID (UUID)" {...register('customerId')} />

      <select {...register('planType', { required: true })}>
        <option value="monthly">Hàng tháng</option>
        <option value="quarterly">Hàng quý</option>
        <option value="yearly">Hàng năm</option>
        <option value="custom">Tùy chỉnh</option>
      </select>
      {errors.planType && <small>{errors.planType.message}</small>}

      <input placeholder="Tổng số tiền *" type="number" step="0.01" min="0" {...register('totalAmount', { required: 'Tổng số tiền là bắt buộc', valueAsNumber: true })} />
      {errors.totalAmount && <small>{errors.totalAmount.message}</small>}

      <input placeholder="Số tiền trả hàng tháng *" type="number" step="0.01" min="0" {...register('monthlyPayment', { required: 'Số tiền trả hàng tháng là bắt buộc', valueAsNumber: true })} />
      {errors.monthlyPayment && <small>{errors.monthlyPayment.message}</small>}

      <input placeholder="Số tháng trả *" type="number" min="1" {...register('numberOfMonths', { required: 'Số tháng trả là bắt buộc', valueAsNumber: true })} />
      {errors.numberOfMonths && <small>{errors.numberOfMonths.message}</small>}

      <input placeholder="Lãi suất (%)" type="number" step="0.01" min="0" max="100" {...register('interestRate', { valueAsNumber: true })} />
      {errors.interestRate && <small>{errors.interestRate.message}</small>}

      <input placeholder="Công ty tài chính" {...register('financeCompany')} />
      {errors.financeCompany && <small>{errors.financeCompany.message}</small>}

      <input placeholder="Ngày bắt đầu *" type="date" {...register('startDate', { required: 'Ngày bắt đầu là bắt buộc' })} />
      {errors.startDate && <small>{errors.startDate.message}</small>}

      <input placeholder="Ngày kết thúc *" type="date" {...register('endDate', { required: 'Ngày kết thúc là bắt buộc' })} />
      {errors.endDate && <small>{errors.endDate.message}</small>}

      <select {...register('status')}>
        <option value="active">Hoạt động</option>
        <option value="completed">Hoàn thành</option>
        <option value="cancelled">Đã hủy</option>
        <option value="overdue">Quá hạn</option>
      </select>

      <textarea placeholder="Ghi chú" rows="3" {...register('notes')} />
      {errors.notes && <small>{errors.notes.message}</small>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Đang tạo...' : 'Tạo kế hoạch trả góp'}
      </button>
    </form>
  );
}

