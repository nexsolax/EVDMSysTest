import React from 'react';
import { useForm } from 'react-hook-form';
import { User, Calendar, Star } from 'lucide-react';
import '../modals/Modal.css';
import './Forms.css';

export default function CustomerForm({ 
  customer = null, 
  mode = 'create', 
  onSubmit, 
  onCancel,
  loading = false 
}) {
  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: customer ? {
      firstName: customer.firstName || '',
      lastName: customer.lastName || '',
      email: customer.email || '',
      phone: customer.phone || '',
      address: customer.address || '',
      city: customer.city || '',
      province: customer.province || '',
      postalCode: customer.postalCode || '',
      dateOfBirth: customer.dateOfBirth ? customer.dateOfBirth.split('T')[0] : '',
      creditScore: customer.creditScore || 0,
      preferredContactMethod: customer.preferredContactMethod || 'EMAIL',
      notes: customer.notes || '',
      isActive: customer.isActive !== undefined ? customer.isActive : true
    } : {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      province: '',
      postalCode: '',
      dateOfBirth: '',
      creditScore: 0,
      preferredContactMethod: 'EMAIL',
      notes: '',
      isActive: true
    }
  });

  const getCreditScoreColor = (score) => {
    if (score >= 750) return '#10b981';
    if (score >= 650) return '#f59e0b';
    return '#ef4444';
  };

  const creditScore = customer?.creditScore || 0;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="firstName">Họ</label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            {...register('firstName', { required: 'Họ là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.firstName && <small className="error">{errors.firstName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="lastName">Tên</label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            {...register('lastName', { required: 'Tên là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.lastName && <small className="error">{errors.lastName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            {...register('email', { 
              required: 'Email là bắt buộc',
              pattern: {
                value: /\S+@\S+\.\S+/,
                message: 'Email không hợp lệ'
              }
            })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.email && <small className="error">{errors.email.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="phone">Số điện thoại</label>
          <input
            type="tel"
            id="phone"
            name="phone"
            {...register('phone')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.phone && <small className="error">{errors.phone.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="address">Địa chỉ</label>
          <input
            type="text"
            id="address"
            name="address"
            {...register('address')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.address && <small className="error">{errors.address.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="city">Thành phố</label>
          <input
            type="text"
            id="city"
            name="city"
            {...register('city')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.city && <small className="error">{errors.city.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="province">Tỉnh/TP</label>
          <input
            type="text"
            id="province"
            name="province"
            {...register('province')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.province && <small className="error">{errors.province.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="postalCode">Mã bưu điện</label>
          <input
            type="text"
            id="postalCode"
            name="postalCode"
            {...register('postalCode')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.postalCode && <small className="error">{errors.postalCode.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="dateOfBirth">Ngày sinh</label>
          <input
            type="date"
            id="dateOfBirth"
            name="dateOfBirth"
            {...register('dateOfBirth')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.dateOfBirth && <small className="error">{errors.dateOfBirth.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="creditScore">Điểm tín dụng</label>
          <input
            type="number"
            id="creditScore"
            name="creditScore"
            {...register('creditScore', { 
              valueAsNumber: true,
              min: { value: 300, message: 'Điểm tín dụng tối thiểu là 300' },
              max: { value: 850, message: 'Điểm tín dụng tối đa là 850' }
            })}
            disabled={mode === 'view'}
            className="form-input"
            min="300"
            max="850"
          />
          {errors.creditScore && <small className="error">{errors.creditScore.message}</small>}
          {mode === 'view' && customer && (
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '8px', 
              marginTop: '4px',
              color: getCreditScoreColor(creditScore)
            }}>
              <Star size={16} />
              <span style={{ fontSize: '12px' }}>
                {creditScore >= 750 ? 'Tốt' : 
                 creditScore >= 650 ? 'Trung bình' : 'Kém'}
              </span>
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="preferredContactMethod">Phương thức liên hệ ưa thích</label>
          <select
            id="preferredContactMethod"
            name="preferredContactMethod"
            {...register('preferredContactMethod')}
            disabled={mode === 'view'}
            className="form-select"
          >
            <option value="EMAIL">Email</option>
            <option value="PHONE">Điện thoại</option>
            <option value="SMS">SMS</option>
          </select>
          {errors.preferredContactMethod && <small className="error">{errors.preferredContactMethod.message}</small>}
        </div>

        <div className="form-group full-width">
          <label htmlFor="notes">Ghi chú</label>
          <textarea
            id="notes"
            name="notes"
            {...register('notes')}
            disabled={mode === 'view'}
            className="form-textarea"
            rows="3"
            placeholder="Ghi chú về khách hàng"
          />
          {errors.notes && <small className="error">{errors.notes.message}</small>}
        </div>

        <div className="form-group">
          <label className="checkbox-label">
            <input
              type="checkbox"
              name="isActive"
              {...register('isActive')}
              disabled={mode === 'view'}
              className="form-checkbox"
            />
            <span>Đang hoạt động</span>
          </label>
        </div>
      </div>

      {mode === 'view' && customer && customer.createdAt && (
        <div className="modal-info">
          <div className="info-item">
            <Calendar size={16} />
            <span>Ngày tạo: {new Date(customer.createdAt).toLocaleDateString('vi-VN')}</span>
          </div>
        </div>
      )}

      <div className="modal-actions">
        <button type="button" className="btn btn-outline" onClick={onCancel}>
          {mode === 'view' ? 'Đóng' : 'Hủy'}
        </button>
        {mode !== 'view' && (
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo mới' : 'Lưu thay đổi'}
          </button>
        )}
      </div>
    </form>
  );
}

