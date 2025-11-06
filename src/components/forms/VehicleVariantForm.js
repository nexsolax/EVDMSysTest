import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { vehicleAPI, imageAPI } from '../../services/api';
import toast from 'react-hot-toast';
import '../modals/Modal.css';
import './Forms.css';

export default function VehicleVariantForm({ variant, mode = 'view', onSubmit, onCancel, loading }) {
  const [models, setModels] = useState([]);
  const { register, handleSubmit, formState: { errors }, reset, watch, setValue } = useForm({
      defaultValues: {
        variantName: '',
        modelId: '',
        priceBase: '',
        batteryCapacity: '',
        rangeKm: '',
        powerKw: '',
        acceleration0100: '',
        topSpeed: '',
        chargingTimeFast: '',
        chargingTimeSlow: '',
        variantImageUrl: '',
        variantImagePath: '',
        isActive: true
      }
  });

  const variantImageUrl = watch('variantImageUrl');
  const variantImagePath = watch('variantImagePath');
  const [selectedFile, setSelectedFile] = useState(null);
  const [localPreview, setLocalPreview] = useState(null);
  const [previewError, setPreviewError] = useState(false);
  const [uploading, setUploading] = useState(false);

  // Xử lý khi chọn file
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        toast.error('Vui lòng chọn file hình ảnh');
        e.target.value = '';
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast.error('Kích thước file không được vượt quá 5MB');
        e.target.value = '';
        return;
      }

      setSelectedFile(file);
      
      // Tạo preview từ file local
      const reader = new FileReader();
      reader.onload = (event) => {
        setLocalPreview(event.target.result);
      };
      reader.readAsDataURL(file);
    } else {
      setSelectedFile(null);
      setLocalPreview(null);
    }
  };

  // Xác định URL ảnh để preview (giống brand logo)
  const getPreviewUrl = () => {
    // Ưu tiên preview từ file local
    if (localPreview) {
      return localPreview;
    }
    // Sau đó là URL
    if (variantImageUrl) {
      return variantImageUrl;
    }
    // Cuối cùng là path
    if (variantImagePath) {
      // Nếu là đường dẫn tuyệt đối (bắt đầu với http/https), dùng trực tiếp
      if (variantImagePath.startsWith('http://') || variantImagePath.startsWith('https://')) {
        return variantImagePath;
      }
      // Nếu là đường dẫn tương đối, giả định là từ public/uploads hoặc server
      return variantImagePath.startsWith('/') ? variantImagePath : `/${variantImagePath}`;
    }
    return null;
  };

  const previewUrl = getPreviewUrl();

  // Reset preview error và file khi URL/path thay đổi từ bên ngoài
  useEffect(() => {
    // Reset error khi có URL/path mới để cho phép thử lại
    setPreviewError(false);
    if (variant && !selectedFile) {
      setLocalPreview(null);
    }
  }, [variantImageUrl, variantImagePath, variant, selectedFile, localPreview]);

  useEffect(() => {
    const loadModels = async () => {
      try {
        const response = await vehicleAPI.getActiveModels();
        setModels(response.data || []);
      } catch (error) {
        console.error('Error loading models:', error);
        toast.error('Không thể tải danh sách dòng xe');
      }
    };
    loadModels();
  }, []);

  // Khi models đã load xong và có variant, đảm bảo modelId được set đúng
  useEffect(() => {
    if (variant && models.length > 0) {
      const modelIdValue = variant.modelId || variant.model?.modelId;
      if (modelIdValue) {
        // Kiểm tra xem modelId có trong danh sách models không
        const modelExists = models.some(m => m.modelId === modelIdValue || String(m.modelId) === String(modelIdValue));
        if (modelExists) {
          setTimeout(() => {
            setValue('modelId', String(modelIdValue));
          }, 100);
        }
      }
    }
  }, [models, variant, setValue]);

  // Update form khi variant data thay đổi
  useEffect(() => {
    if (!variant) {
      // Reset form nếu không có variant (create mode) - chỉ cho phép xe điện
      reset({
        variantName: '',
        modelId: '',
        priceBase: '',
        batteryCapacity: '',
        rangeKm: '',
        powerKw: '',
        acceleration0100: '',
        topSpeed: '',
        chargingTimeFast: '',
        chargingTimeSlow: '',
        variantImageUrl: '',
        variantImagePath: '',
        isActive: true
      });
      setSelectedFile(null);
      setLocalPreview(null);
    } else {
      // Map dữ liệu từ backend sang form
      // Convert modelId sang string để match với select value
      const modelIdValue = variant.modelId || variant.model?.modelId;
      const formData = {
        variantName: variant.variantName || '',
        modelId: modelIdValue ? String(modelIdValue) : '',
        // Xử lý số: giữ nguyên nếu có giá trị, không set '' nếu là 0
        priceBase: (variant.priceBase !== null && variant.priceBase !== undefined ? variant.priceBase : 
                   (variant.basePrice !== null && variant.basePrice !== undefined ? variant.basePrice : '')),
        batteryCapacity: variant.batteryCapacity !== null && variant.batteryCapacity !== undefined ? variant.batteryCapacity : '',
        rangeKm: variant.rangeKm !== null && variant.rangeKm !== undefined ? variant.rangeKm : '',
        powerKw: variant.powerKw !== null && variant.powerKw !== undefined ? variant.powerKw : '',
        acceleration0100: variant.acceleration0100 !== null && variant.acceleration0100 !== undefined ? variant.acceleration0100 : '',
        topSpeed: variant.topSpeed !== null && variant.topSpeed !== undefined ? variant.topSpeed : '',
        chargingTimeFast: variant.chargingTimeFast !== null && variant.chargingTimeFast !== undefined ? variant.chargingTimeFast : '',
        chargingTimeSlow: variant.chargingTimeSlow !== null && variant.chargingTimeSlow !== undefined ? variant.chargingTimeSlow : '',
        variantImageUrl: variant.variantImageUrl || '',
        variantImagePath: variant.variantImagePath || '',
        isActive: variant.isActive !== undefined ? variant.isActive : true
      };
      console.log('Setting form data from variant:', formData, 'Original variant:', variant);
      
      // Reset form với tất cả fields
      reset(formData);
      
      // Đảm bảo modelId được set đúng sau khi models đã load
      if (modelIdValue && models.length > 0) {
        // Delay một chút để form được render xong
        setTimeout(() => {
          setValue('modelId', String(modelIdValue));
        }, 100);
      }
      
      // Reset file selection khi load variant mới
      setSelectedFile(null);
      setLocalPreview(null);
    }
  }, [variant, reset, models]);

  const submitForm = async (data) => {
    try {
      // Nếu có file mới được chọn, upload file trước
      if (selectedFile) {
        setUploading(true);
        try {
          const variantId = mode === 'edit' && variant?.variantId ? variant.variantId : null;
          const uploadResponse = await imageAPI.uploadVariantImage(selectedFile, variantId);
          console.log('Upload response:', uploadResponse);
          
          // Theo API documentation: response có uploadResult.url hoặc updateResult.imageUrl
          // Nếu có variantId, API đã tự động update vào DB, nhưng vẫn cần submit lại để đảm bảo
          if (uploadResponse.data?.updateResult?.imageUrl) {
            // Nếu có variantId, API đã update và trả về imageUrl
            data.variantImageUrl = uploadResponse.data.updateResult.imageUrl;
            // Extract path từ URL nếu cần
            const imageUrl = uploadResponse.data.updateResult.imageUrl;
            if (imageUrl && !imageUrl.startsWith('http')) {
              data.variantImagePath = imageUrl.replace('/uploads/', '');
            }
          } else if (uploadResponse.data?.uploadResult?.url) {
            // Nếu không có variantId, chỉ có uploadResult.url
            const imageUrl = uploadResponse.data.uploadResult.url;
            data.variantImageUrl = imageUrl;
            // Extract path từ URL
            if (imageUrl && !imageUrl.startsWith('http')) {
              data.variantImagePath = imageUrl.replace('/uploads/', '');
            }
          }
        } catch (uploadError) {
          console.error('Error uploading variant image:', uploadError);
          toast.error('Không thể upload hình ảnh. Vui lòng thử lại.');
          setUploading(false);
          return;
        } finally {
          setUploading(false);
        }
      }

      // Chuẩn hóa dữ liệu theo API: chỉ submit các field có giá trị
      // Convert modelId từ string sang number nếu cần
      if (data.modelId && typeof data.modelId === 'string') {
        data.modelId = parseInt(data.modelId, 10);
      }

      // Remove empty strings và undefined values (trừ basePrice - required)
      // Không submit variantImageUrl và variantImagePath nếu rỗng (chỉ submit sau khi upload)
      const cleanedData = {};
      Object.keys(data).forEach(key => {
        const value = data[key];
        // Bỏ qua các field không có trong API request
        if (key === 'variantImageFile') {
          return; // File input không submit vào API
        }
        
        // Giữ lại các field required hoặc có giá trị
        if (key === 'modelId' || key === 'variantName' || key === 'priceBase') {
          cleanedData[key] = value;
        } else if (key === 'variantImageUrl' || key === 'variantImagePath') {
          // Chỉ submit image fields nếu có giá trị (sau khi upload)
          if (value && value !== null && value !== undefined && value !== '') {
            cleanedData[key] = value;
          }
        } else if (key === 'isActive') {
          // Boolean: luôn gửi (kể cả false)
          cleanedData[key] = value !== undefined ? value : true;
        } else if (value !== null && value !== undefined && value !== '') {
          // Các field khác: chỉ gửi nếu có giá trị
          cleanedData[key] = value;
        }
        // Bỏ qua: null, undefined, empty string cho optional fields
      });

      // Convert priceBase sang number nếu là string
      if (cleanedData.priceBase && typeof cleanedData.priceBase === 'string') {
        cleanedData.priceBase = parseFloat(cleanedData.priceBase);
      }

      // Convert các số khác nếu cần - chỉ các field có trong API
      // Theo FIELD_REFERENCE_GUIDE.md line 110-116:
      // - powerKw: Integer (optional)
      // - rangeKm: Integer (optional)
      // - topSpeed: Integer (optional)
      // - chargingTimeFast: Integer (optional)
      // - chargingTimeSlow: Integer (optional)
      // - batteryCapacity: BigDecimal (optional) - hỗ trợ decimal
      // - acceleration0100: BigDecimal (optional)
      const integerFields = ['powerKw', 'rangeKm', 'topSpeed', 'chargingTimeFast', 'chargingTimeSlow'];
      integerFields.forEach(field => {
        if (cleanedData[field] !== undefined && cleanedData[field] !== null && cleanedData[field] !== '') {
          cleanedData[field] = typeof cleanedData[field] === 'string' ? parseInt(cleanedData[field], 10) : Math.round(cleanedData[field]);
        }
      });
      
      // BigDecimal fields (có thể có decimal)
      const decimalFields = ['batteryCapacity', 'acceleration0100'];
      decimalFields.forEach(field => {
        if (cleanedData[field] !== undefined && cleanedData[field] !== null && cleanedData[field] !== '') {
          cleanedData[field] = typeof cleanedData[field] === 'string' ? parseFloat(cleanedData[field]) : cleanedData[field];
        }
      });

      console.log('Submitting cleaned variant data:', cleanedData);
      onSubmit?.(cleanedData);
    } catch (error) {
      console.error('Error in submitForm:', error);
      toast.error('Có lỗi xảy ra khi xử lý form');
    }
  };

  return (
    <form onSubmit={handleSubmit(submitForm)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="variantName">Tên phiên bản *</label>
          <input
            type="text"
            id="variantName"
            {...register('variantName', { required: 'Tên phiên bản là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.variantName && <small className="error">{errors.variantName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="modelId">Dòng xe *</label>
          {mode === 'view' ? (
            // Hiển thị text khi view mode để đảm bảo hiển thị đúng
            <div className="form-input" style={{ background: '#f9fafb', cursor: 'not-allowed' }}>
              {(() => {
                const modelIdValue = variant?.modelId || variant?.model?.modelId;
                if (modelIdValue && models.length > 0) {
                  const selectedModel = models.find(m => m.modelId === modelIdValue || String(m.modelId) === String(modelIdValue));
                  return selectedModel?.modelName || variant?.model?.modelName || variant?.modelName || 'N/A';
                }
                return variant?.model?.modelName || variant?.modelName || 'N/A';
              })()}
            </div>
          ) : (
            <select
              id="modelId"
              {...register('modelId', { required: 'Dòng xe là bắt buộc' })}
              className="form-select"
            >
              <option value="">Chọn dòng xe</option>
              {models.map(model => (
                <option key={model.modelId} value={model.modelId}>
                  {model.modelName}
                </option>
              ))}
            </select>
          )}
          {errors.modelId && <small className="error">{errors.modelId.message}</small>}
        </div>


        <div className="form-group">
          <label htmlFor="priceBase">Giá cơ bản (VNĐ) *</label>
          <input
            type="number"
            id="priceBase"
            step="0.01"
            min="0"
            {...register('priceBase', { 
              required: 'Giá cơ bản là bắt buộc',
              valueAsNumber: true,
              min: { value: 0, message: 'Giá phải >= 0' }
            })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.priceBase && <small className="error">{errors.priceBase.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="batteryCapacity">Dung lượng pin (kWh)</label>
          <input
            type="number"
            id="batteryCapacity"
            step="0.01"
            min="0"
            {...register('batteryCapacity', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="rangeKm">Tầm hoạt động (km)</label>
          <input
            type="number"
            id="rangeKm"
            step="0.01"
            min="0"
            {...register('rangeKm', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="powerKw">Công suất (kW)</label>
          <input
            type="number"
            id="powerKw"
            step="0.01"
            min="0"
            {...register('powerKw', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="acceleration0100">Tăng tốc 0-100 (giây)</label>
          <input
            type="number"
            id="acceleration0100"
            step="0.1"
            min="0"
            {...register('acceleration0100', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="topSpeed">Vận tốc tối đa (km/h)</label>
          <input
            type="number"
            id="topSpeed"
            step="0.01"
            min="0"
            {...register('topSpeed', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="chargingTimeFast">Thời gian sạc nhanh (phút)</label>
          <input
            type="number"
            id="chargingTimeFast"
            step="0.01"
            min="0"
            {...register('chargingTimeFast', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="chargingTimeSlow">Thời gian sạc chậm (phút)</label>
          <input
            type="number"
            id="chargingTimeSlow"
            step="0.01"
            min="0"
            {...register('chargingTimeSlow', { valueAsNumber: true })}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        {/* URL hình ảnh - chỉ hiển thị trong view mode, ẩn trong create/update */}
        {mode === 'view' && (
          <div className="form-group full-width">
            <label htmlFor="variantImageUrl">URL Hình ảnh</label>
            <input
              type="url"
              id="variantImageUrl"
              {...register('variantImageUrl')}
              disabled={true}
              className="form-input"
              placeholder="https://example.com/variant-image.png"
            />
            {errors.variantImageUrl && <small className="error">{errors.variantImageUrl.message}</small>}
          </div>
        )}

        <div className="form-group full-width">
          <label htmlFor="variantImageFile">Chọn file hình ảnh</label>
          <input
            type="file"
            id="variantImageFile"
            accept="image/*"
            disabled={mode === 'view' || uploading}
            className="form-input"
            onChange={handleFileChange}
          />
          {selectedFile && (
            <small style={{ display: 'block', marginTop: '4px', color: '#6b7280' }}>
              Đã chọn: {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
            </small>
          )}
          {uploading && (
            <small style={{ display: 'block', marginTop: '4px', color: '#3b82f6' }}>
              Đang upload hình ảnh...
            </small>
          )}
        </div>

        {/* Đường dẫn file hình ảnh - chỉ hiển thị trong view mode, ẩn trong create/update */}
        {mode === 'view' && (
          <div className="form-group full-width">
            <label htmlFor="variantImagePath">Đường dẫn file hình ảnh (sau khi upload)</label>
            <input
              type="text"
              id="variantImagePath"
              {...register('variantImagePath')}
              disabled={true}
              className="form-input"
              placeholder="/uploads/variants/variant-image.png"
              readOnly
            />
            {errors.variantImagePath && <small className="error">{errors.variantImagePath.message}</small>}
          </div>
        )}

        {/* Image Preview */}
        {previewUrl && (
          <div className="form-group full-width">
            <label>Xem trước hình ảnh</label>
            <div className="logo-preview-container">
              {!previewError ? (
                <img
                  src={previewUrl}
                  alt="Variant Image Preview"
                  className="logo-preview"
                  onError={() => {
                    setPreviewError(true);
                  }}
                  onLoad={() => {
                    setPreviewError(false);
                  }}
                />
              ) : (
                <div className="logo-preview-error">
                  <p>Không thể tải ảnh</p>
                  <small>Vui lòng kiểm tra lại URL hoặc đường dẫn</small>
                </div>
              )}
            </div>
          </div>
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
          <button type="submit" className="btn btn-primary" disabled={loading || uploading}>
            {loading || uploading ? 'Đang lưu...' : 'Lưu'}
          </button>
        )}
      </div>
    </form>
  );
}

