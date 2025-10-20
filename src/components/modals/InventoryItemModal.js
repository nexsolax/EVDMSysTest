import React, { useEffect, useState } from 'react';
import { X, Save, Package, Hash } from 'lucide-react';
import { inventoryAPI, warehouseAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const InventoryItemModal = ({ item, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    vehicleId: '',
    warehouseId: '',
    colorId: '',
    vin: '',
    status: 'AVAILABLE',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    const init = async () => {
      await Promise.all([loadWarehouses(), loadVehicles()]);
      if (item) {
        if (mode === 'edit') {
          await loadItemDetails();
        } else {
          setFormData({
            vehicleId: item.vehicle?.vehicleId || item.vehicleId || '',
            warehouseId: item.warehouse?.warehouseId || item.warehouseId || '',
            colorId: item.color?.colorId || item.colorId || '',
            vin: item.vin || '',
            status: item.status || 'AVAILABLE',
            notes: item.notes || ''
          });
        }
      }
    };
    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, item, mode]);

  const loadWarehouses = async () => {
    try {
      const res = await warehouseAPI.getActiveWarehouses?.();
      setWarehouses(res?.data || []);
    } catch {}
  };

  const loadVehicles = async () => {
    try {
      const res = await vehicleAPI.getActiveVehicles?.();
      setVehicles(res?.data || []);
    } catch {}
  };

  const loadItemDetails = async () => {
    try {
      setLoading(true);
      const res = await inventoryAPI.getInventoryItem(item.inventoryId);
      const i = res.data;
      setFormData({
        vehicleId: i.vehicle?.vehicleId || i.vehicleId || '',
        warehouseId: i.warehouse?.warehouseId || i.warehouseId || '',
        colorId: i.color?.colorId || i.colorId || '',
        vin: i.vin || '',
        status: i.status || 'AVAILABLE',
        notes: i.notes || ''
      });
    } catch (e) {
      console.error('Error loading inventory item:', e);
      toast.error('Không thể tải thông tin tồn kho');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;
    try {
      setLoading(true);
      await onSave(item.inventoryId, formData);
      onClose();
    } catch (e) {
      // upstream toast
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
            <h2>{mode === 'view' ? 'Xem chi tiết tồn kho' : 'Chỉnh sửa tồn kho'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="vehicleId">Xe</label>
              <select id="vehicleId" name="vehicleId" className="form-select" value={formData.vehicleId} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="">Chọn xe</option>
                {vehicles.map((v) => (
                  <option key={v.vehicleId} value={v.vehicleId}>
                    {v.brand?.brandName} {v.model?.modelName} {v.variant?.variantName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="warehouseId">Kho</label>
              <select id="warehouseId" name="warehouseId" className="form-select" value={formData.warehouseId} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="">Chọn kho</option>
                {warehouses.map((w) => (
                  <option key={w.warehouseId} value={w.warehouseId}>
                    {w.warehouseName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="colorId">Màu</label>
              <input id="colorId" name="colorId" className="form-input" value={formData.colorId} onChange={handleChange} disabled={mode === 'view'} placeholder="ID màu (tạm thời)" />
            </div>

            <div className="form-group">
              <label htmlFor="vin">VIN</label>
              <div className="input-with-icon">
                <Hash size={16} />
                <input id="vin" name="vin" className="form-input" value={formData.vin} onChange={handleChange} disabled={mode === 'view'} required />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select id="status" name="status" className="form-select" value={formData.status} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="AVAILABLE">Có sẵn</option>
                <option value="RESERVED">Đã đặt</option>
                <option value="SOLD">Đã bán</option>
                <option value="MAINTENANCE">Bảo trì</option>
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="notes">Ghi chú</label>
              <textarea id="notes" name="notes" className="form-input" rows={3} value={formData.notes} onChange={handleChange} disabled={mode === 'view'} placeholder="Ghi chú về tồn kho..." />
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

export default InventoryItemModal;
