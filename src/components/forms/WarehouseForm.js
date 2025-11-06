import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import '../modals/Modal.css';
import './Forms.css';

export default function WarehouseForm({ warehouse, mode = 'view', onSubmit, onCancel, loading }) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    defaultValues: {
      warehouseName: '',
      warehouseCode: '',
      address: '',
      city: '',
      province: '',
      postalCode: '',
      phone: '',
      email: '',
      capacity: '',
      isActive: true
    }
  });

  useEffect(() => {
    if (warehouse && mode !== 'create') {
      reset({
        warehouseName: warehouse.warehouseName || '',
        warehouseCode: warehouse.warehouseCode || '',
        address: warehouse.address || '',
        city: warehouse.city || '',
        province: warehouse.province || '',
        postalCode: warehouse.postalCode || '',
        phone: warehouse.phone || '',
        email: warehouse.email || '',
        capacity: warehouse.capacity || '',
        isActive: warehouse.isActive !== undefined ? warehouse.isActive : true
      });
    } else if (mode === 'create') {
      reset({
        warehouseName: '',
        warehouseCode: '',
        address: '',
        city: '',
        province: '',
        postalCode: '',
        phone: '',
        email: '',
        capacity: '',
        isActive: true
      });
    }
  }, [warehouse, mode, reset]);

  const submitForm = (data) => {
    // Required fields (theo INPUT_ORDER_GUIDE.md line 93-99)
    const submitData = {
      warehouseName: data.warehouseName?.trim() || '',
      warehouseCode: data.warehouseCode?.trim() || '',
      address: data.address?.trim() || '',
      city: data.city?.trim() || '',
      province: data.province?.trim() || ''
    };
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (data.postalCode?.trim()) {
      submitData.postalCode = data.postalCode.trim();
    }
    if (data.phone?.trim()) {
      submitData.phone = data.phone.trim();
    }
    if (data.email?.trim()) {
      submitData.email = data.email.trim();
    }
    if (data.capacity !== undefined && data.capacity !== null && data.capacity !== '') {
      submitData.capacity = parseInt(data.capacity, 10);
    }
    if (data.isActive !== undefined) {
      submitData.isActive = data.isActive;
    }
    
    onSubmit?.(submitData);
  };

  return (
    <form onSubmit={handleSubmit(submitForm)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="warehouseName">Tên kho *</label>
          <input
            id="warehouseName"
            {...register('warehouseName', { required: 'Tên kho là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.warehouseName && <small className="error">{errors.warehouseName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="warehouseCode">Mã kho *</label>
          <input
            id="warehouseCode"
            {...register('warehouseCode', { required: 'Mã kho là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.warehouseCode && <small className="error">{errors.warehouseCode.message}</small>}
        </div>

        <div className="form-group full-width">
          <label htmlFor="address">Địa chỉ *</label>
          <input
            id="address"
            {...register('address', { required: 'Địa chỉ là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.address && <small className="error">{errors.address.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="city">Thành phố *</label>
          <input
            id="city"
            {...register('city', { required: 'Thành phố là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.city && <small className="error">{errors.city.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="province">Tỉnh/TP *</label>
          <input
            id="province"
            {...register('province', { required: 'Tỉnh/TP là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.province && <small className="error">{errors.province.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="postalCode">Mã bưu chính</label>
          <input
            id="postalCode"
            {...register('postalCode')}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="phone">Điện thoại</label>
          <input
            id="phone"
            type="tel"
            {...register('phone')}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            {...register('email')}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.email && <small className="error">{errors.email.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="capacity">Sức chứa</label>
          <input
            id="capacity"
            type="number"
            min="0"
            {...register('capacity', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label className="checkbox-label">
            <input
              type="checkbox"
              className="form-checkbox"
              {...register('isActive')}
              disabled={mode === 'view'}
            />
            <span>Đang hoạt động</span>
          </label>
        </div>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn btn-outline" onClick={onCancel}>
          {mode === 'view' ? 'Đóng' : 'Hủy'}
        </button>
        {mode !== 'view' && (
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo kho' : 'Lưu thay đổi'}
          </button>
        )}
      </div>
    </form>
  );
}

