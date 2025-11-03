import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function DealerTargetForm({ baseUrl = '', onCreated, defaultDealerId = null }) {
  const currentYear = new Date().getFullYear();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      dealerId: defaultDealerId,
      targetYear: currentYear,
      targetType: 'sales',
      targetScope: 'dealer',
      targetStatus: 'active'
    }
  });

  const onSubmit = async (values) => {
    const created = await apiFetch(`${baseUrl}/api/dealer-targets`, { method: 'POST', body: values });
    onCreated?.(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Tên mục tiêu *" {...register('targetName', { required: 'Tên mục tiêu là bắt buộc' })} />
      {errors.targetName && <small>{errors.targetName.message}</small>}

      <textarea placeholder="Mô tả" rows="3" {...register('description')} />
      {errors.description && <small>{errors.description.message}</small>}

      <input placeholder="Dealer ID (UUID)" {...register('dealerId')} />

      <select {...register('targetType', { required: true })}>
        <option value="sales">Bán hàng</option>
        <option value="revenue">Doanh thu</option>
        <option value="customer">Khách hàng</option>
        <option value="inventory">Tồn kho</option>
      </select>
      {errors.targetType && <small>{errors.targetType.message}</small>}

      <select {...register('targetScope', { required: true })}>
        <option value="global">Toàn hệ thống</option>
        <option value="dealer">Đại lý</option>
      </select>
      {errors.targetScope && <small>{errors.targetScope.message}</small>}

      <input placeholder="Năm mục tiêu *" type="number" min="2020" max="2100" {...register('targetYear', { required: 'Năm mục tiêu là bắt buộc', valueAsNumber: true })} />
      {errors.targetYear && <small>{errors.targetYear.message}</small>}

      <input placeholder="Tháng mục tiêu (1-12)" type="number" min="1" max="12" {...register('targetMonth', { valueAsNumber: true })} />
      {errors.targetMonth && <small>{errors.targetMonth.message}</small>}

      <input placeholder="Giá trị mục tiêu *" type="number" step="0.01" min="0" {...register('targetValue', { required: 'Giá trị mục tiêu là bắt buộc', valueAsNumber: true })} />
      {errors.targetValue && <small>{errors.targetValue.message}</small>}

      <input placeholder="Giá trị thực tế" type="number" step="0.01" min="0" {...register('actualValue', { valueAsNumber: true })} />
      {errors.actualValue && <small>{errors.actualValue.message}</small>}

      <input placeholder="Đơn vị" {...register('unit')} />
      {errors.unit && <small>{errors.unit.message}</small>}

      <select {...register('targetStatus')}>
        <option value="active">Hoạt động</option>
        <option value="completed">Hoàn thành</option>
        <option value="cancelled">Đã hủy</option>
      </select>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Đang tạo...' : 'Tạo mục tiêu đại lý'}
      </button>
    </form>
  );
}

