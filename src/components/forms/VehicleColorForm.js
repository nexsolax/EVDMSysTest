import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { imageAPI } from '../../services/api';
import toast from 'react-hot-toast';
import '../modals/Modal.css';
import './Forms.css';

export default function VehicleColorForm({ color, mode = 'view', onSubmit, onCancel, loading }) {
  const { register, handleSubmit, watch, setValue, formState: { errors }, reset } = useForm({
    defaultValues: {
      colorName: '',
      colorCode: '',
      colorSwatchUrl: '',
      colorSwatchPath: '',
      isActive: true
    }
  });

  const colorSwatchUrl = watch('colorSwatchUrl');
  const colorSwatchPath = watch('colorSwatchPath');
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
    if (colorSwatchUrl) {
      return colorSwatchUrl;
    }
    // Cuối cùng là path
    if (colorSwatchPath) {
      // Nếu là đường dẫn tuyệt đối (bắt đầu với http/https), dùng trực tiếp
      if (colorSwatchPath.startsWith('http://') || colorSwatchPath.startsWith('https://')) {
        return colorSwatchPath;
      }
      // Nếu là đường dẫn tương đối, giả định là từ public/uploads hoặc server
      return colorSwatchPath.startsWith('/') ? colorSwatchPath : `/${colorSwatchPath}`;
    }
    return null;
  };

  const previewUrl = getPreviewUrl();

  // Reset preview error và file khi URL/path thay đổi từ bên ngoài
  useEffect(() => {
    if (color && !selectedFile) {
      setPreviewError(false);
      setLocalPreview(null);
    }
  }, [colorSwatchUrl, colorSwatchPath, color, selectedFile]);

  // Update form khi color data thay đổi
  useEffect(() => {
    if (!color) {
      // Reset form nếu không có color (create mode)
      reset({
        colorName: '',
        colorCode: '',
        colorSwatchUrl: '',
        colorSwatchPath: '',
        isActive: true
      });
    } else {
      // Map dữ liệu từ backend sang form
      const formData = {
        colorName: color.colorName || '',
        colorCode: color.colorCode || '',
        colorSwatchUrl: color.colorSwatchUrl || '',
        colorSwatchPath: color.colorSwatchPath || '',
        isActive: color.isActive !== undefined ? color.isActive : true
      };
      console.log('Setting form data from color:', formData, 'Original color:', color);
      
      // Reset form với tất cả fields
      reset(formData);
    }
  }, [color, reset]);

  const submitForm = async (data) => {
    try {
      setUploading(true);

      // Nếu có file mới được chọn, upload trước
      if (selectedFile) {
        const colorId = color?.colorId || null;
        console.log('Uploading color swatch, colorId:', colorId);
        const uploadResponse = await imageAPI.uploadColorSwatch(selectedFile, colorId);
        console.log('Upload response:', uploadResponse);
        
        if (uploadResponse?.data) {
          // Cập nhật colorSwatchUrl và colorSwatchPath từ response
          data.colorSwatchUrl = uploadResponse.data.imageUrl || uploadResponse.data.url || uploadResponse.data.colorSwatchUrl || '';
          data.colorSwatchPath = uploadResponse.data.imagePath || uploadResponse.data.path || uploadResponse.data.colorSwatchPath || '';
          console.log('Updated data with swatch URLs:', { 
            colorSwatchUrl: data.colorSwatchUrl, 
            colorSwatchPath: data.colorSwatchPath 
          });
        }
      }

      // Chuẩn hóa data trước khi submit (loại bỏ undefined, giữ lại empty string cho swatch)
      const submitData = {
        colorName: data.colorName?.trim() || '',
        colorCode: data.colorCode?.trim() || '',
        colorSwatchUrl: data.colorSwatchUrl || '',
        colorSwatchPath: data.colorSwatchPath || '',
        isActive: data.isActive !== undefined ? data.isActive : true
      };
      
      console.log('Submitting color data:', submitData);
      // Submit data
      await onSubmit?.(submitData);
      setUploading(false);
    } catch (error) {
      console.error('Upload/submit error:', error);
      toast.error('Lỗi khi lưu màu sắc: ' + (error.response?.data?.message || error.message));
      setUploading(false);
      throw error; // Re-throw để modal có thể xử lý
    }
  };

  return (
    <form onSubmit={handleSubmit(submitForm)} className="modal-form">
      <div className="form-grid">
        <div className="form-group">
          <label htmlFor="colorName">Tên màu *</label>
          <input
            type="text"
            id="colorName"
            {...register('colorName', { required: 'Tên màu là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
          />
          {errors.colorName && <small className="error">{errors.colorName.message}</small>}
        </div>

        <div className="form-group">
          <label htmlFor="colorCode">Mã màu *</label>
          <input
            type="text"
            id="colorCode"
            {...register('colorCode', { required: 'Mã màu là bắt buộc' })}
            disabled={mode === 'view'}
            className="form-input"
            placeholder="VD: RED001, BLU002..."
          />
          {errors.colorCode && <small className="error">{errors.colorCode.message}</small>}
        </div>

        {/* Image Upload Fields - chỉ hiển thị trong view mode */}
        {mode === 'view' && (
          <>
            <div className="form-group">
              <label htmlFor="colorSwatchUrl">URL Hình ảnh màu</label>
              <input
                type="text"
                id="colorSwatchUrl"
                {...register('colorSwatchUrl')}
                disabled={true}
                className="form-input"
                readOnly
              />
            </div>
            <div className="form-group">
              <label htmlFor="colorSwatchPath">Đường dẫn hình ảnh</label>
              <input
                type="text"
                id="colorSwatchPath"
                {...register('colorSwatchPath')}
                disabled={true}
                className="form-input"
                readOnly
              />
            </div>
          </>
        )}

        {/* Image Upload - chỉ hiển thị trong create/edit mode */}
        {mode !== 'view' && (
          <div className="form-group">
            <label htmlFor="colorSwatchFile">Hình ảnh màu (Swatch)</label>
            <input
              type="file"
              id="colorSwatchFile"
              accept="image/*"
              onChange={handleFileChange}
              className="form-input"
              disabled={uploading}
            />
            <small className="hint">Chọn file hình ảnh để hiển thị màu (JPG, PNG, max 5MB)</small>
            
            {/* Preview section */}
            {previewUrl && (
              <div className="logo-preview-container">
                {previewError ? (
                  <div className="logo-preview-error">
                    <span>Không thể tải hình ảnh</span>
                  </div>
                ) : (
                  <img
                    src={previewUrl}
                    alt="Color swatch preview"
                    className="logo-preview"
                    onError={() => setPreviewError(true)}
                    onLoad={() => setPreviewError(false)}
                  />
                )}
              </div>
            )}
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

