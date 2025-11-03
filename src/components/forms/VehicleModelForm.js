import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { vehicleAPI } from '../../services/api';
import '../modals/Modal.css';
import './Forms.css';

export default function VehicleModelForm({ model, mode = 'view', onSubmit, onCancel, loading }) {
  const [brands, setBrands] = useState([]);
  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm({
    defaultValues: {
      modelName: '',
      brandId: '',
      modelYear: '',
      vehicleType: '',
      description: '',
      specifications: '',
      modelImageUrl: '',
      modelImagePath: '',
      isActive: true
    }
  });
  
  const brandIdValue = watch('brandId');

  useEffect(() => {
    const loadBrands = async () => {
      try {
        // Load tất cả brands nếu ở view mode để đảm bảo có brand hiển thị
        const apiCall = mode === 'view' ? vehicleAPI.getBrands() : vehicleAPI.getActiveBrands();
        const response = await apiCall;
        const brandsData = response.data || [];
        setBrands(brandsData);
        console.log('Loaded brands:', brandsData);
      } catch (error) {
        console.error('Error loading brands:', error);
        // Fallback to active brands nếu getBrands lỗi
        try {
          const fallbackResponse = await vehicleAPI.getActiveBrands();
          setBrands(fallbackResponse.data || []);
        } catch (fallbackError) {
          console.error('Fallback brands load also failed:', fallbackError);
        }
      }
    };
    loadBrands();
  }, [mode]);

  // Update form khi model data hoặc brands thay đổi
  useEffect(() => {
    if (!model) {
      // Reset form nếu không có model (create mode)
      reset({
        modelName: '',
        brandId: '',
        modelYear: '',
        vehicleType: '',
        description: '',
        specifications: '',
        modelImageUrl: '',
        modelImagePath: '',
        isActive: true
      });
    } else {
      // Map dữ liệu từ backend sang form
      const brandIdFromModel = model.brandId || model.brand?.brandId;
      const formData = {
        modelName: model.modelName || '',
        brandId: brandIdFromModel ? brandIdFromModel.toString() : '', // Convert to string for select
        modelYear: model.modelYear || model.year || '',
        vehicleType: model.vehicleType || model.bodyType || '',
        description: model.description || '',
        specifications: model.specifications || (typeof model.specifications === 'string' ? model.specifications : ''),
        modelImageUrl: model.modelImageUrl || '',
        modelImagePath: model.modelImagePath || '',
        isActive: model.isActive !== undefined ? model.isActive : true
      };
      console.log('Setting form data from model:', formData, 'Original model:', model);
      
      // Reset form với tất cả fields bao gồm brandId
      reset(formData);
      
      // Đảm bảo brandId được set đúng khi brands đã load
      if (formData.brandId && brands.length > 0) {
        // Verify brandId exists in brands list
        const brandExists = brands.some(b => b.brandId.toString() === formData.brandId);
        if (brandExists) {
          console.log('Setting brandId:', formData.brandId, 'Brands available:', brands.map(b => ({ id: b.brandId.toString(), name: b.brandName })));
          // Force update brandId sau reset để đảm bảo select hiển thị đúng
          setTimeout(() => {
            setValue('brandId', formData.brandId, { shouldValidate: false });
          }, 0);
        } else {
          console.warn('BrandId not found in brands list:', formData.brandId);
        }
      }
    }
  }, [model, brands, reset, setValue]); // Depend on brands để đợi brands load xong

  const submitForm = (data) => {
    // Chuẩn hóa dữ liệu trước khi submit
    const normalizedData = {
      brandId: parseInt(data.brandId, 10), // Convert string to number
      modelName: data.modelName?.trim() || '',
      modelYear: data.modelYear ? parseInt(data.modelYear, 10) : null, // Ensure modelYear is number (required)
      isActive: data.isActive !== undefined ? data.isActive : true
    };
    
    // Optional fields - chỉ thêm nếu có giá trị
    if (data.vehicleType?.trim()) {
      normalizedData.vehicleType = data.vehicleType.trim();
    }
    if (data.description?.trim()) {
      normalizedData.description = data.description.trim();
    }
    if (data.specifications?.trim()) {
      normalizedData.specifications = data.specifications.trim();
    }
    if (data.modelImageUrl?.trim()) {
      normalizedData.modelImageUrl = data.modelImageUrl.trim();
    }
    if (data.modelImagePath?.trim()) {
      normalizedData.modelImagePath = data.modelImagePath.trim();
    }
    
    console.log('Submitting model data:', normalizedData);
    onSubmit?.(normalizedData);
  };

  return (
    <form onSubmit={handleSubmit(submitForm)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="modelName">Tên dòng xe *</label>
          <input
            type="text"
            id="modelName"
            {...register('modelName', { required: 'Tên dòng xe là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.modelName && <small className="error">{errors.modelName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="brandId">Thương hiệu *</label>
          {mode === 'view' ? (
            // Hiển thị text khi view mode để đảm bảo hiển thị đúng
            <div className="form-input" style={{ background: '#f9fafb', cursor: 'not-allowed' }}>
              {brandIdValue && brands.length > 0 
                ? (brands.find(b => b.brandId.toString() === brandIdValue.toString())?.brandName || 'N/A')
                : (model?.brand?.brandName || model?.brandName || 'N/A')}
            </div>
          ) : (
            <select
              id="brandId"
              {...register('brandId', { required: 'Thương hiệu là bắt buộc' })}
              className="form-select"
            >
              <option value="">Chọn thương hiệu</option>
              {brands.map(brand => (
                <option key={brand.brandId} value={brand.brandId.toString()}>
                  {brand.brandName}
                </option>
              ))}
            </select>
          )}
          {errors.brandId && <small className="error">{errors.brandId.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="modelYear">Năm sản xuất *</label>
          <input
            type="number"
            id="modelYear"
            min="1900"
            max={new Date().getFullYear() + 1}
            {...register('modelYear', { 
              required: 'Năm sản xuất là bắt buộc',
              valueAsNumber: true,
              min: { value: 1900, message: 'Năm phải >= 1900' },
              max: { value: new Date().getFullYear() + 1, message: `Năm không được vượt quá ${new Date().getFullYear() + 1}` }
            })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.modelYear && <small className="error">{errors.modelYear.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="vehicleType">Loại xe</label>
          <select
            id="vehicleType"
            {...register('vehicleType')}
            disabled={mode === 'view'}
            className="form-select"
          >
            <option value="">Chọn loại xe</option>
            <option value="SEDAN">Sedan</option>
            <option value="SUV">SUV</option>
            <option value="HATCHBACK">Hatchback</option>
            <option value="COUPE">Coupe</option>
            <option value="TRUCK">Truck</option>
            <option value="MPV">MPV</option>
          </select>
        </div>

        <div className="form-group full-width">
          <label htmlFor="description">Mô tả</label>
          <textarea
            id="description"
            {...register('description')}
            disabled={mode === 'view'}
            className="form-input"
            rows={4}
            placeholder="Mô tả về dòng xe..."
          />
        </div>

        <div className="form-group full-width">
          <label htmlFor="specifications">Thông số kỹ thuật (JSON)</label>
          <textarea
            id="specifications"
            {...register('specifications')}
            disabled={mode === 'view'}
            className="form-input"
            rows={4}
            placeholder='{"doors": 4, "seats": 5}'
          />
          <small className="hint">Nhập JSON format, ví dụ: {"{"}"doors": 4, "seats": 5{"}"}</small>
        </div>

        {/* Image fields - chỉ hiển thị trong view mode */}
        {mode === 'view' && (
          <>
            <div className="form-group full-width">
              <label htmlFor="modelImageUrl">URL Hình ảnh model</label>
              <input
                type="text"
                id="modelImageUrl"
                {...register('modelImageUrl')}
                disabled={true}
                className="form-input"
                readOnly
              />
            </div>
            <div className="form-group full-width">
              <label htmlFor="modelImagePath">Đường dẫn hình ảnh</label>
              <input
                type="text"
                id="modelImagePath"
                {...register('modelImagePath')}
                disabled={true}
                className="form-input"
                readOnly
              />
            </div>
          </>
        )}

        <div className="form-group">
          <label className="checkbox-label">
            <input
              type="checkbox"
              {...register('isActive')}
              disabled={mode === 'view'}
              className="form-checkbox"
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
            {loading ? 'Đang lưu...' : 'Lưu'}
          </button>
        )}
      </div>
    </form>
  );
}

