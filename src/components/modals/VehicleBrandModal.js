import React, { useState, useEffect } from 'react';
import { X, Save, Car, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleBrandModal = ({ brand, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    brandName: '',
    country: '',
    foundedYear: '',
    description: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && brand) {
      if (mode === 'edit') {
        loadBrandDetails();
      } else {
        setFormData({
          brandName: brand.brandName || '',
          country: brand.country || '',
          foundedYear: brand.foundedYear || '',
          description: brand.description || '',
          isActive: brand.isActive || false
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, brand, mode]);

  const loadBrandDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getBrand(brand.brandId);
      const brandData = response.data;
      setFormData({
        brandName: brandData.brandName || '',
        country: brandData.country || '',
        foundedYear: brandData.foundedYear || '',
        description: brandData.description || '',
        isActive: brandData.isActive || false
      });
    } catch (error) {
      console.error('Error loading brand details:', error);
      toast.error('Không thể tải thông tin thương hiệu');
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
      await onSave(brand.brandId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving brand:', error);
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
            <Car size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết thương hiệu' : 'Chỉnh sửa thương hiệu'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="brandName">Tên thương hiệu</label>
              <input
                type="text"
                id="brandName"
                name="brandName"
                value={formData.brandName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="country">Quốc gia</label>
              <input
                type="text"
                id="country"
                name="country"
                value={formData.country}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="foundedYear">Năm thành lập</label>
              <input
                type="number"
                id="foundedYear"
                name="foundedYear"
                value={formData.foundedYear}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="1800"
                max={new Date().getFullYear()}
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
                placeholder="Mô tả về thương hiệu..."
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
                <span>Ngày tạo: {brand.createdAt ? new Date(brand.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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

export default VehicleBrandModal;
