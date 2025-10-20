import React, { useState, useEffect } from 'react';
import { X, Save, Settings, DollarSign } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleVariantModal = ({ variant, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    variantName: '',
    modelId: '',
    engineType: '',
    transmission: '',
    fuelType: '',
    basePrice: '',
    description: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);
  const [models, setModels] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadModels();
      if (variant) {
        if (mode === 'edit') {
          loadVariantDetails();
        } else {
          setFormData({
            variantName: variant.variantName || '',
            modelId: variant.model?.modelId || variant.modelId || '',
            engineType: variant.engineType || '',
            transmission: variant.transmission || '',
            fuelType: variant.fuelType || '',
            basePrice: variant.basePrice || '',
            description: variant.description || '',
            isActive: variant.isActive || false
          });
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, variant, mode]);

  const loadModels = async () => {
    try {
      const response = await vehicleAPI.getActiveModels();
      setModels(response.data || []);
    } catch (error) {
      console.error('Error loading models:', error);
    }
  };

  const loadVariantDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getVariant(variant.variantId);
      const variantData = response.data;
      setFormData({
        variantName: variantData.variantName || '',
        modelId: variantData.model?.modelId || variantData.modelId || '',
        engineType: variantData.engineType || '',
        transmission: variantData.transmission || '',
        fuelType: variantData.fuelType || '',
        basePrice: variantData.basePrice || '',
        description: variantData.description || '',
        isActive: variantData.isActive || false
      });
    } catch (error) {
      console.error('Error loading variant details:', error);
      toast.error('Không thể tải thông tin phiên bản');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(variant.variantId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving variant:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <Settings size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết phiên bản' : 'Chỉnh sửa phiên bản'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="variantName">Tên phiên bản</label>
              <input
                type="text"
                id="variantName"
                name="variantName"
                value={formData.variantName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="modelId">Dòng xe</label>
              <select
                id="modelId"
                name="modelId"
                value={formData.modelId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn dòng xe</option>
                {models.map(model => (
                  <option key={model.modelId} value={model.modelId}>
                    {model.modelName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="engineType">Loại động cơ</label>
              <select
                id="engineType"
                name="engineType"
                value={formData.engineType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn loại động cơ</option>
                <option value="ELECTRIC">Điện</option>
                <option value="HYBRID">Hybrid</option>
                <option value="PETROL">Xăng</option>
                <option value="DIESEL">Diesel</option>
                <option value="GAS">Gas</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="transmission">Hộp số</label>
              <select
                id="transmission"
                name="transmission"
                value={formData.transmission}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn hộp số</option>
                <option value="MANUAL">Số sàn</option>
                <option value="AUTOMATIC">Số tự động</option>
                <option value="CVT">CVT</option>
                <option value="SEMI_AUTOMATIC">Bán tự động</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="fuelType">Loại nhiên liệu</label>
              <select
                id="fuelType"
                name="fuelType"
                value={formData.fuelType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn loại nhiên liệu</option>
                <option value="ELECTRIC">Điện</option>
                <option value="HYBRID">Hybrid</option>
                <option value="PETROL">Xăng</option>
                <option value="DIESEL">Diesel</option>
                <option value="GAS">Gas</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="basePrice">Giá cơ bản (VNĐ)</label>
              <input
                type="number"
                id="basePrice"
                name="basePrice"
                value={formData.basePrice}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                placeholder="Nhập giá cơ bản..."
              />
            </div>

            <div className="form-group full-width">
              <label htmlFor="description">Mô tả</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                rows={4}
                placeholder="Mô tả về phiên bản..."
              />
            </div>

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-checkbox"
                />
                <span>Đang hoạt động</span>
              </label>
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <DollarSign size={16} />
                <span>Giá cơ bản: {formData.basePrice ? new Intl.NumberFormat('vi-VN').format(formData.basePrice) + ' VNĐ' : 'Chưa có'}</span>
              </div>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {mode === 'edit' && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default VehicleVariantModal;
