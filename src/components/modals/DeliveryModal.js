import React, { useEffect, useState } from 'react';
import { X, Save, Truck, Calendar, MapPin } from 'lucide-react';
import { deliveryAPI, customerAPI, orderAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const DeliveryModal = ({ delivery, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    orderId: '',
    vehicleId: '',
    deliveryAddress: '',
    scheduledDate: '',
    status: 'SCHEDULED',
    isActive: true,
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    if (!isOpen) return;

    const init = async () => {
      try {
        await Promise.all([loadCustomers(), loadOrders(), loadVehicles()]);
        if (delivery) {
          if (mode === 'edit') {
            await loadDeliveryDetails();
          } else {
            setFormData({
              customerId: delivery.customer?.customerId || delivery.customerId || '',
              orderId: delivery.order?.orderId || delivery.orderId || '',
              vehicleId: delivery.vehicle?.vehicleId || delivery.vehicleId || '',
              deliveryAddress: delivery.deliveryAddress || '',
              scheduledDate: delivery.scheduledDate ? delivery.scheduledDate.slice(0, 10) : '',
              status: delivery.status || 'SCHEDULED',
              isActive: delivery.isActive !== undefined ? delivery.isActive : true,
              notes: delivery.notes || ''
            });
          }
        }
      } catch {
        // handled in loaders
      }
    };

    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, delivery, mode]);

  const loadCustomers = async () => {
    try {
      const res = await customerAPI.getActiveCustomers?.();
      setCustomers(res?.data || []);
    } catch (e) {
      // Optional list; ignore errors
    }
  };

  const loadOrders = async () => {
    try {
      const res = await orderAPI.getActiveOrders?.();
      setOrders(res?.data || []);
    } catch (e) {
      // Optional list; ignore errors
    }
  };

  const loadVehicles = async () => {
    try {
      const res = await vehicleAPI.getActiveVehicles?.();
      setVehicles(res?.data || []);
    } catch (e) {
      // Optional list; ignore errors
    }
  };

  const loadDeliveryDetails = async () => {
    try {
      setLoading(true);
      const res = await deliveryAPI.getDelivery(delivery.deliveryId);
      const d = res.data;
      setFormData({
        customerId: d.customer?.customerId || d.customerId || '',
        orderId: d.order?.orderId || d.orderId || '',
        vehicleId: d.vehicle?.vehicleId || d.vehicleId || '',
        deliveryAddress: d.deliveryAddress || '',
        scheduledDate: d.scheduledDate ? d.scheduledDate.slice(0, 10) : '',
        status: d.status || 'SCHEDULED',
        notes: d.notes || ''
      });
    } catch (error) {
      console.error('Error loading delivery:', error);
      toast.error('Không thể tải thông tin giao xe');
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
      await onSave(delivery.deliveryId, formData);
      onClose();
    } catch (error) {
      // toast already shown upstream
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
            <Truck size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết giao xe' : 'Chỉnh sửa giao xe'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="customerId">Khách hàng</label>
              <select
                id="customerId"
                name="customerId"
                value={formData.customerId}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn khách hàng</option>
                {customers.map((c) => (
                  <option key={c.customerId} value={c.customerId}>
                    {c.firstName} {c.lastName} - {c.email}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="orderId">Đơn hàng</label>
              <select
                id="orderId"
                name="orderId"
                value={formData.orderId}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="">Chọn đơn hàng (tuỳ chọn)</option>
                {orders.map((o) => (
                  <option key={o.orderId} value={o.orderId}>
                    #{o.orderId} - {o.customer?.firstName} {o.customer?.lastName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="vehicleId">Xe</label>
              <select
                id="vehicleId"
                name="vehicleId"
                value={formData.vehicleId}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn xe</option>
                {vehicles.map((v) => (
                  <option key={v.vehicleId} value={v.vehicleId}>
                    {v.brand?.brandName} {v.model?.modelName} {v.variant?.variantName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="deliveryAddress">Địa chỉ giao</label>
              <div className="address-input">
                <MapPin size={16} />
                <input
                  type="text"
                  id="deliveryAddress"
                  name="deliveryAddress"
                  value={formData.deliveryAddress}
                  onChange={handleChange}
                  disabled={mode === 'view'}
                  className="form-input"
                  placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="scheduledDate">Ngày giao dự kiến</label>
              <input
                type="date"
                id="scheduledDate"
                name="scheduledDate"
                value={formData.scheduledDate}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="SCHEDULED">Đã lên lịch</option>
                <option value="IN_TRANSIT">Đang giao</option>
                <option value="DELIVERED">Đã giao</option>
                <option value="FAILED">Giao thất bại</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
            </div>

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleChange}
                  disabled={mode === 'view'}
                />
                <span>Đang hoạt động</span>
              </label>
            </div>

            <div className="form-group full-width">
              <label htmlFor="notes">Ghi chú</label>
              <textarea
                id="notes"
                name="notes"
                value={formData.notes}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-input"
                rows={3}
                placeholder="Ghi chú thêm về giao xe..."
              />
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>
                  Ngày dự kiến: {delivery.scheduledDate ? new Date(delivery.scheduledDate).toLocaleDateString('vi-VN') : 'N/A'}
                </span>
              </div>
              <div className="info-item">
                <Truck size={16} />
                <span>Số giao xe: {delivery.deliveryNumber || 'N/A'}</span>
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

export default DeliveryModal;
