import React from 'react';
import { useForm } from 'react-hook-form';
import { apiFetch } from '../../utils/apiClient';
import '../modals/Modal.css';
import './Forms.css';

export default function RegisterForm({ baseUrl = '', onRegistered }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    // Required fields (giả định theo form structure)
    const submitData = {
      username: values.username?.trim() || '',
      email: values.email?.trim() || '',
      password: values.password || '',
      firstName: values.firstName?.trim() || '',
      lastName: values.lastName?.trim() || ''
    };
    
    // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
    if (values.phone?.trim()) {
      submitData.phone = values.phone.trim();
    }
    if (values.dealerId) {
      submitData.dealerId = values.dealerId;
    }
    if (values.userType?.trim()) {
      submitData.userType = values.userType.trim(); // ADMIN, EVM_STAFF, DEALER_STAFF, CUSTOMER_SUPPORT
    }
    if (values.status?.trim()) {
      submitData.status = values.status.trim(); // ACTIVE, INACTIVE, LOCKED
    }
    
    const result = await apiFetch(`${baseUrl}/api/auth/register`, { method: 'POST', body: submitData });
    onRegistered?.(result);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Tên đăng nhập *" {...register('username')} />
      {errors.username && <small>{errors.username.message}</small>}

      <input placeholder="Email *" type="email" {...register('email')} />
      {errors.email && <small>{errors.email.message}</small>}

      <input placeholder="Mật khẩu *" type="password" {...register('password')} />
      {errors.password && <small>{errors.password.message}</small>}

      <input placeholder="Họ *" {...register('firstName')} />
      {errors.firstName && <small>{errors.firstName.message}</small>}

      <input placeholder="Tên *" {...register('lastName')} />
      {errors.lastName && <small>{errors.lastName.message}</small>}

      <input placeholder="Số điện thoại" {...register('phone')} />
      {errors.phone && <small>{errors.phone.message}</small>}

      <input placeholder="Dealer ID (UUID)" {...register('dealerId')} />
      
      <select {...register('userType')}>
        <option value="">Chọn loại người dùng</option>
        <option value="ADMIN">Admin</option>
        <option value="EVM_STAFF">Nhân viên EVM</option>
        <option value="DEALER_STAFF">Nhân viên đại lý</option>
        <option value="CUSTOMER_SUPPORT">Hỗ trợ khách hàng</option>
      </select>
      {errors.userType && <small>{errors.userType.message}</small>}

      <select {...register('status')}>
        <option value="ACTIVE">Kích hoạt</option>
        <option value="INACTIVE">Vô hiệu hóa</option>
        <option value="LOCKED">Khóa</option>
      </select>
      {errors.status && <small>{errors.status.message}</small>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Đang đăng ký...' : 'Đăng ký'}
      </button>
    </form>
  );
}

