import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function PromotionForm({ baseUrl = '', onCreated }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      promotionType: 'discount_percentage',
      status: 'active'
    }
  });

  const onSubmit = async (values) => {
    const created = await apiFetch(`${baseUrl}/api/promotions`, { method: 'POST', body: values });
    onCreated?.(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Tên khuyến mãi *" {...register('promotionName', { required: 'Tên khuyến mãi là bắt buộc' })} />
      {errors.promotionName && <small>{errors.promotionName.message}</small>}

      <select {...register('promotionType', { required: true })}>
        <option value="discount_percentage">Giảm phần trăm</option>
        <option value="discount_amount">Giảm số tiền</option>
        <option value="free_gift">Quà tặng miễn phí</option>
        <option value="buy_one_get_one">Mua một tặng một</option>
      </select>
      {errors.promotionType && <small>{errors.promotionType.message}</small>}

      <input placeholder="Phần trăm giảm giá" type="number" step="0.01" min="0" max="100" {...register('discountPercent', { valueAsNumber: true })} />
      {errors.discountPercent && <small>{errors.discountPercent.message}</small>}

      <input placeholder="Số tiền giảm (VNĐ)" type="number" step="0.01" min="0" {...register('discountAmount', { valueAsNumber: true })} />
      {errors.discountAmount && <small>{errors.discountAmount.message}</small>}

      <input placeholder="Ngày bắt đầu *" type="date" {...register('startDate', { required: 'Ngày bắt đầu là bắt buộc' })} />
      {errors.startDate && <small>{errors.startDate.message}</small>}

      <input placeholder="Ngày kết thúc *" type="date" {...register('endDate', { required: 'Ngày kết thúc là bắt buộc' })} />
      {errors.endDate && <small>{errors.endDate.message}</small>}

      <textarea placeholder="Mô tả" rows="3" {...register('description')} />
      {errors.description && <small>{errors.description.message}</small>}

      <select {...register('status')}>
        <option value="active">Hoạt động</option>
        <option value="inactive">Không hoạt động</option>
        <option value="expired">Hết hạn</option>
      </select>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Đang tạo...' : 'Tạo khuyến mãi'}
      </button>
    </form>
  );
}

