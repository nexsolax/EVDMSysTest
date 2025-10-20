import React, { useState, useEffect } from 'react';
import { X, Save, Palette } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleColorModal = ({ color, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    colorName: '',
    colorCode: '',
    hexCode: '',
    description: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && color) {
      if (mode === 'edit') {
        loadColorDetails();
      } else {
        setFormData({
          colorName: color.colorName || '',
          colorCode: color.colorCode || '',
          hexCode: color.hexCode || '',
          description: color.description || '',
          isActive: color.isActive || false
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, color, mode]);

  const loadColorDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getColor(color.colorId);
      const colorData = response.data;
      setFormData({
        colorName: colorData.colorName || '',
        colorCode: colorData.colorCode || '',
        hexCode: colorData.hexCode || '',
        description: colorData.description || '',
        isActive: colorData.isActive || false
      });
    } catch (error) {
      console.error('Error loading color details:', error);
      toast.error('Không thể tải thông tin màu sắc');
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
      await onSave(color.colorId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving color:', error);
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
            <Palette size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết màu sắc' : 'Chỉnh sửa màu sắc'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="colorName">Tên màu</label>
              <input
                type="text"
                id="colorName"
                name="colorName"
                value={formData.colorName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="colorCode">Mã màu</label>
              <input
                type="text"
                id="colorCode"
                name="colorCode"
                value={formData.colorCode}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="VD: RED001, BLU002..."
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="hexCode">Mã hex</label>
              <div className="color-input-group">
                <input
                  type="color"
                  id="hexCode"
                  name="hexCode"
                  value={formData.hexCode}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="color-picker"
                />
                <input
                  type="text"
                  value={formData.hexCode}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-input"
                  placeholder="#FF0000"
                  pattern="^#[0-9A-Fa-f]{6}$"
                />
              </div>
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
                placeholder="Mô tả về màu sắc..."
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

          {mode === 'view' && formData.hexCode && (
            <div className="modal-info">
              <div className="info-item">
                <div 
                  className="color-preview" 
                  style={{ backgroundColor: formData.hexCode }}
                ></div>
                <span>Màu hiển thị: {formData.colorName}</span>
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

export default VehicleColorModal;
