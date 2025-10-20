import React, { useEffect, useState } from 'react';
import { X, Save, Building2 } from 'lucide-react';
import { warehouseAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const WarehouseModal = ({ warehouse, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
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
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    const init = async () => {
      if (warehouse) {
        if (mode === 'edit') {
          await loadWarehouseDetails();
        } else {
          setFormData({
            warehouseName: warehouse.warehouseName || '',
            warehouseCode: warehouse.warehouseCode || '',
            address: warehouse.address || '',
            city: warehouse.city || '',
            province: warehouse.province || '',
            postalCode: warehouse.postalCode || '',
            phone: warehouse.phone || '',
            email: warehouse.email || '',
            capacity: warehouse.capacity || '',
            isActive: warehouse.isActive ?? true
          });
        }
      }
    };
    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, warehouse, mode]);

  const loadWarehouseDetails = async () => {
    try {
      setLoading(true);
      const res = await warehouseAPI.getWarehouse(warehouse.warehouseId);
      const w = res.data;
      setFormData({
        warehouseName: w.warehouseName || '',
        warehouseCode: w.warehouseCode || '',
        address: w.address || '',
        city: w.city || '',
        province: w.province || '',
        postalCode: w.postalCode || '',
        phone: w.phone || '',
        email: w.email || '',
        capacity: w.capacity || '',
        isActive: w.isActive ?? true
      });
    } catch (e) {
      console.error('Error loading warehouse:', e);
      toast.error('Không thể tải thông tin kho');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;
    try {
      setLoading(true);
      await onSave(warehouse.warehouseId, formData);
      onClose();
    } catch (e) {
      // toast upstream
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
            <Building2 size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết kho' : 'Chỉnh sửa kho'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="warehouseName">Tên kho</label>
              <input id="warehouseName" name="warehouseName" value={formData.warehouseName} onChange={handleChange} disabled={mode === 'view'} className="form-input" required />
            </div>
            <div className="form-group">
              <label htmlFor="warehouseCode">Mã kho</label>
              <input id="warehouseCode" name="warehouseCode" value={formData.warehouseCode} onChange={handleChange} disabled={mode === 'view'} className="form-input" required />
            </div>
            <div className="form-group full-width">
              <label htmlFor="address">Địa chỉ</label>
              <input id="address" name="address" value={formData.address} onChange={handleChange} disabled={mode === 'view'} className="form-input" required />
            </div>
            <div className="form-group">
              <label htmlFor="city">Thành phố</label>
              <input id="city" name="city" value={formData.city} onChange={handleChange} disabled={mode === 'view'} className="form-input" required />
            </div>
            <div className="form-group">
              <label htmlFor="province">Tỉnh/TP</label>
              <input id="province" name="province" value={formData.province} onChange={handleChange} disabled={mode === 'view'} className="form-input" required />
            </div>
            <div className="form-group">
              <label htmlFor="postalCode">Mã bưu chính</label>
              <input id="postalCode" name="postalCode" value={formData.postalCode} onChange={handleChange} disabled={mode === 'view'} className="form-input" />
            </div>
            <div className="form-group">
              <label htmlFor="phone">Điện thoại</label>
              <input id="phone" name="phone" value={formData.phone} onChange={handleChange} disabled={mode === 'view'} className="form-input" />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} disabled={mode === 'view'} className="form-input" />
            </div>
            <div className="form-group">
              <label htmlFor="capacity">Sức chứa</label>
              <input id="capacity" name="capacity" type="number" min="0" value={formData.capacity} onChange={handleChange} disabled={mode === 'view'} className="form-input" />
            </div>
            <div className="form-group">
              <label className="checkbox-label">
                <input type="checkbox" className="form-checkbox" name="isActive" checked={formData.isActive} onChange={handleChange} disabled={mode === 'view'} />
                <span>Đang hoạt động</span>
              </label>
            </div>
          </div>

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

export default WarehouseModal;
