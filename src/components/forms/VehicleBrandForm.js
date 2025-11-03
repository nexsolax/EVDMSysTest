import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { imageAPI } from '../../services/api';
import toast from 'react-hot-toast';
import '../modals/Modal.css';
import './Forms.css';

export default function VehicleBrandForm({ brand, mode = 'view', onSubmit, onCancel, loading }) {
  const { register, handleSubmit, formState: { errors }, reset, watch } = useForm({
    defaultValues: {
      brandName: '',
      country: '',
      foundedYear: '',
      brandLogoUrl: '',
      brandLogoPath: '',
      isActive: true
    }
  });

  const brandLogoUrl = watch('brandLogoUrl');
  const brandLogoPath = watch('brandLogoPath');
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

  // Xác định URL ảnh để preview
  const getPreviewUrl = () => {
    // Ưu tiên preview từ file local
    if (localPreview) {
      return localPreview;
    }
    // Sau đó là URL
    if (brandLogoUrl) {
      return brandLogoUrl;
    }
    // Cuối cùng là path
    if (brandLogoPath) {
      // Nếu là đường dẫn tuyệt đối (bắt đầu với http/https), dùng trực tiếp
      if (brandLogoPath.startsWith('http://') || brandLogoPath.startsWith('https://')) {
        return brandLogoPath;
      }
      // Nếu là đường dẫn tương đối, giả định là từ public/uploads hoặc server
      return brandLogoPath.startsWith('/') ? brandLogoPath : `/${brandLogoPath}`;
    }
    return null;
  };

  const previewUrl = getPreviewUrl();

  // Reset preview error và file khi URL/path thay đổi từ bên ngoài
  useEffect(() => {
    if (brand && !selectedFile) {
      setPreviewError(false);
      setLocalPreview(null);
    }
  }, [brandLogoUrl, brandLogoPath, brand, selectedFile]);

  // Update form khi brand data thay đổi
  useEffect(() => {
    if (!brand) {
      // Reset form nếu không có brand (create mode)
      reset({
        brandName: '',
        country: '',
        foundedYear: '',
        brandLogoUrl: '',
        brandLogoPath: '',
        isActive: true
      });
      setSelectedFile(null);
      setLocalPreview(null);
    } else {
      // Map dữ liệu từ backend sang form - xử lý null/undefined
      const formData = {
        brandName: brand.brandName || '',
        country: brand.country || '',
        foundedYear: brand.foundedYear || '',
        brandLogoUrl: brand.brandLogoUrl || '',
        brandLogoPath: brand.brandLogoPath || '',
        isActive: brand.isActive !== undefined ? brand.isActive : true
      };
      console.log('Setting form data from brand:', formData);
      
      // Reset form với tất cả fields
      reset(formData);
      // Reset file selection khi load brand mới
      setSelectedFile(null);
      setLocalPreview(null);
    }
  }, [brand, reset]);

  const submitForm = async (data) => {
    try {
      // Nếu có file mới được chọn, upload file trước
      if (selectedFile) {
        setUploading(true);
        try {
          const brandId = mode === 'edit' && brand?.brandId ? brand.brandId : null;
          const uploadResponse = await imageAPI.uploadVehicleBrandImage(selectedFile, brandId);
          console.log('Upload response:', uploadResponse);
          
          // Cập nhật brandLogoPath từ response (có thể là path hoặc URL)
          if (uploadResponse.data?.path) {
            data.brandLogoPath = uploadResponse.data.path;
          } else if (uploadResponse.data?.url) {
            data.brandLogoUrl = uploadResponse.data.url;
            // Clear path nếu có URL
            data.brandLogoPath = '';
          } else if (uploadResponse.data?.brandLogoPath) {
            data.brandLogoPath = uploadResponse.data.brandLogoPath;
          } else if (uploadResponse.data?.brandLogoUrl) {
            data.brandLogoUrl = uploadResponse.data.brandLogoUrl;
            data.brandLogoPath = '';
          }
        } catch (uploadError) {
          console.error('Error uploading logo:', uploadError);
          toast.error('Không thể upload logo. Vui lòng thử lại.');
          setUploading(false);
          return;
        } finally {
          setUploading(false);
        }
      }

      // Submit data (selectedFile không có trong data vì nó là state riêng)
      onSubmit?.(data);
    } catch (error) {
      console.error('Error in submitForm:', error);
      toast.error('Có lỗi xảy ra khi xử lý form');
    }
  };

  return (
    <form onSubmit={handleSubmit(submitForm)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="brandName">Tên thương hiệu *</label>
          <input
            type="text"
            id="brandName"
            {...register('brandName', { required: 'Tên thương hiệu là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.brandName && <small className="error">{errors.brandName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="country">Quốc gia</label>
          <input
            type="text"
            id="country"
            {...register('country')}
            disabled={mode === 'view'}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="foundedYear">Năm thành lập</label>
          <input
            type="number"
            id="foundedYear"
            min="1800"
            max={new Date().getFullYear()}
            {...register('foundedYear', { 
              valueAsNumber: true,
              min: { value: 1800, message: 'Năm phải >= 1800' },
              max: { value: new Date().getFullYear(), message: `Năm không được vượt quá ${new Date().getFullYear()}` }
            })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.foundedYear && <small className="error">{errors.foundedYear.message}</small>}
        </div>

        <div className="form-group full-width">
          {/* URL Logo - chỉ hiển thị trong view mode, ẩn trong create/update */}
          {mode === 'view' && (
            <>
              <label htmlFor="brandLogoUrl">URL Logo</label>
              <input
                type="url"
                id="brandLogoUrl"
                {...register('brandLogoUrl')}
                disabled={true}
                className="form-input"
                placeholder="https://example.com/logo.png"
              />
              {errors.brandLogoUrl && <small className="error">{errors.brandLogoUrl.message}</small>}
            </>
          )}
        </div>

        <div className="form-group full-width">
          <label htmlFor="brandLogoFile">Chọn file logo</label>
          <input
            type="file"
            id="brandLogoFile"
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
              Đang upload logo...
            </small>
          )}
        </div>

        {/* Đường dẫn file logo - chỉ hiển thị trong view mode, ẩn trong create/update */}
        {mode === 'view' && (
          <div className="form-group full-width">
            <label htmlFor="brandLogoPath">Đường dẫn file logo (sau khi upload)</label>
            <input
              type="text"
              id="brandLogoPath"
              {...register('brandLogoPath')}
              disabled={true}
              className="form-input"
              placeholder="/uploads/logos/brand-logo.png"
              readOnly
            />
            {errors.brandLogoPath && <small className="error">{errors.brandLogoPath.message}</small>}
          </div>
        )}

        {/* Logo Preview */}
        {previewUrl && (
          <div className="form-group full-width">
            <label>Xem trước logo</label>
            <div className="logo-preview-container">
              {!previewError ? (
                <img
                  src={previewUrl}
                  alt="Brand Logo Preview"
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

