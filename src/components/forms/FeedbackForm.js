import React from 'react';
import { useForm } from 'react-hook-form';
import '../modals/Modal.css';
import './Forms.css';
import { apiFetch } from '../../utils/apiClient';

export default function FeedbackForm({ baseUrl = '', onSubmitted, defaultCustomerId = null, onCancel }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      customerId: defaultCustomerId,
      rating: 5
    }
  });

  const onSubmit = async (values) => {
    if (onSubmitted) {
      await onSubmitted(values);
    } else {
      const result = await apiFetch(`${baseUrl}/api/customer-feedbacks`, { method: 'POST', body: values });
      onSubmitted?.(result);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Tiêu đề *" {...register('subject')} />
      {errors.subject && <small>{errors.subject.message}</small>}

      <textarea placeholder="Nội dung phản hồi *" rows="5" {...register('message')} />
      {errors.message && <small>{errors.message.message}</small>}

      <div>
        <label>Đánh giá *</label>
        <select {...register('rating', { valueAsNumber: true })}>
          <option value={5}>5 - Xuất sắc</option>
          <option value={4}>4 - Tốt</option>
          <option value={3}>3 - Trung bình</option>
          <option value={2}>2 - Kém</option>
          <option value={1}>1 - Rất kém</option>
        </select>
        {errors.rating && <small>{errors.rating.message}</small>}
      </div>

      <input placeholder="Customer ID (UUID)" {...register('customerId')} />
      <input placeholder="Email liên hệ" type="email" {...register('contactEmail')} />
      {errors.contactEmail && <small>{errors.contactEmail.message}</small>}

      <div className="form-actions">
        {onCancel && (
          <button type="button" className="btn btn-secondary" onClick={onCancel}>
            Hủy
          </button>
        )}
        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Đang gửi...' : 'Gửi phản hồi'}
        </button>
      </div>
    </form>
  );
}

