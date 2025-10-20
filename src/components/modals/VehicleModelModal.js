import React, { useState, useEffect } from 'react';
import { X, Save, Package, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleModelModal = ({ model, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    modelName: '',
    brandId: '',
    year: '',
    vehicleType: '',
    description: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);
  const [brands, setBrands] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadBrands();
      if (model) {
        if (mode === 'edit') {
          loadModelDetails();
        } else {
          setFormData({
            modelName: model.modelName || '',
            brandId: model.brand?.brandId || model.brandId || '',
            year: model.year || '',
            vehicleType: model.vehicleType || '',
            description: model.description || '',
            isActive: model.isActive || false
          });
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, model, mode]);

  const loadBrands = async () => {
    try {
      const response = await vehicleAPI.getActiveBrands();
      setBrands(response.data || []);
    } catch (error) {
      console.error('Error loading brands:', error);
    }
  };

  const loadModelDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getModel(model.modelId);
      const modelData = response.data;
      setFormData({
        modelName: modelData.modelName || '',
        brandId: modelData.brand?.brandId || modelData.brandId || '',
        year: modelData.year || '',
        vehicleType: modelData.vehicleType || '',
        description: modelData.description || '',
        isActive: modelData.isActive || false
      });
    } catch (error) {
      console.error('Error loading model details:', error);
      toast.error('Không thể tải thông tin dòng xe');
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
      await onSave(model.modelId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving model:', error);
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
            <Package size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết dòng xe' : 'Chỉnh sửa dòng xe'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="modelName">Tên dòng xe</label>
              <input
                type="text"
                id="modelName"
                name="modelName"
                value={formData.modelName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="brandId">Thương hiệu</label>
              <select
                id="brandId"
                name="brandId"
                value={formData.brandId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn thương hiệu</option>
                {brands.map(brand => (
                  <option key={brand.brandId} value={brand.brandId}>
                    {brand.brandName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="year">Năm sản xuất</label>
              <input
                type="number"
                id="year"
                name="year"
                value={formData.year}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="1900"
                max={new Date().getFullYear() + 1}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="vehicleType">Loại xe</label>
              <select
                id="vehicleType"
                name="vehicleType"
                value={formData.vehicleType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn loại xe</option>
                <option value="SEDAN">Sedan</option>
                <option value="SUV">SUV</option>
                <option value="HATCHBACK">Hatchback</option>
                <option value="COUPE">Coupe</option>
                <option value="CONVERTIBLE">Convertible</option>
                <option value="TRUCK">Truck</option>
                <option value="VAN">Van</option>
                <option value="MOTORCYCLE">Motorcycle</option>
              </select>
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
                placeholder="Mô tả về dòng xe..."
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
                <Calendar size={16} />
                <span>Ngày tạo: {model.createdAt ? new Date(model.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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

export default VehicleModelModal;
