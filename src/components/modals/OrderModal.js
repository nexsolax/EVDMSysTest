import React, { useState, useEffect } from 'react';
import { X, Save, ShoppingCart, DollarSign, Calendar, FileText } from 'lucide-react';
import { orderAPI, customerAPI, vehicleAPI, quotationAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const OrderModal = ({ order, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    quotationId: '',
    vehicleId: '',
    orderDate: '',
    deliveryDate: '',
    orderAmount: '',
    depositAmount: '',
    remainingAmount: '',
    status: 'PENDING',
    isActive: true,
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [quotations, setQuotations] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadCustomers();
      loadVehicles();
      loadQuotations();
      if (order) {
        if (mode === 'edit') {
          loadOrderDetails();
        } else {
          setFormData({
            customerId: order.customer?.customerId || order.customerId || '',
            quotationId: order.quotation?.quotationId || order.quotationId || '',
            vehicleId: order.vehicle?.vehicleId || order.vehicleId || '',
            orderDate: order.orderDate || '',
            deliveryDate: order.deliveryDate || '',
            orderAmount: order.orderAmount || '',
            depositAmount: order.depositAmount || '',
            remainingAmount: order.remainingAmount || '',
            status: order.status || 'PENDING',
            isActive: order.isActive !== undefined ? order.isActive : true,
            notes: order.notes || ''
          });
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, order, mode]);

  const loadCustomers = async () => {
    try {
      const response = await customerAPI.getActiveCustomers();
      setCustomers(response.data || []);
    } catch (error) {
      console.error('Error loading customers:', error);
    }
  };

  const loadVehicles = async () => {
    try {
      const response = await vehicleAPI.getActiveVehicles();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadQuotations = async () => {
    try {
      const response = await quotationAPI.getActiveQuotations();
      setQuotations(response.data || []);
    } catch (error) {
      console.error('Error loading quotations:', error);
    }
  };

  const loadOrderDetails = async () => {
    try {
      setLoading(true);
      const response = await orderAPI.getOrder(order.orderId);
      const orderData = response.data;
      setFormData({
        customerId: orderData.customer?.customerId || orderData.customerId || '',
        quotationId: orderData.quotation?.quotationId || orderData.quotationId || '',
        vehicleId: orderData.vehicle?.vehicleId || orderData.vehicleId || '',
        orderDate: orderData.orderDate || '',
        deliveryDate: orderData.deliveryDate || '',
        orderAmount: orderData.orderAmount || '',
        depositAmount: orderData.depositAmount || '',
        remainingAmount: orderData.remainingAmount || '',
        status: orderData.status || 'PENDING',
        notes: orderData.notes || ''
      });
    } catch (error) {
      console.error('Error loading order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Auto calculate remaining amount
    if (name === 'orderAmount' || name === 'depositAmount') {
      const orderAmount = parseFloat(name === 'orderAmount' ? value : formData.orderAmount) || 0;
      const depositAmount = parseFloat(name === 'depositAmount' ? value : formData.depositAmount) || 0;
      const remainingAmount = orderAmount - depositAmount;
      setFormData(prev => ({
        ...prev,
        remainingAmount: remainingAmount.toString()
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(order.orderId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving order:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container large">
        <div className="modal-header">
          <div className="modal-title">
            <ShoppingCart size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết đơn hàng' : 'Chỉnh sửa đơn hàng'}</h2>
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
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn khách hàng</option>
                {customers.map(customer => (
                  <option key={customer.customerId} value={customer.customerId}>
                    {customer.firstName} {customer.lastName} - {customer.email}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="quotationId">Báo giá</label>
              <select
                id="quotationId"
                name="quotationId"
                value={formData.quotationId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="">Chọn báo giá (tùy chọn)</option>
                {quotations.map(quotation => (
                  <option key={quotation.quotationId} value={quotation.quotationId}>
                    #{quotation.quotationId} - {quotation.customer?.firstName} {quotation.customer?.lastName}
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
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn xe</option>
                {vehicles.map(vehicle => (
                  <option key={vehicle.vehicleId} value={vehicle.vehicleId}>
                    {vehicle.brand?.brandName} {vehicle.model?.modelName} {vehicle.variant?.variantName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="orderDate">Ngày đặt hàng</label>
              <input
                type="date"
                id="orderDate"
                name="orderDate"
                value={formData.orderDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="deliveryDate">Ngày giao hàng dự kiến</label>
              <input
                type="date"
                id="deliveryDate"
                name="deliveryDate"
                value={formData.deliveryDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="orderAmount">Tổng giá trị đơn hàng (VNĐ)</label>
              <input
                type="number"
                id="orderAmount"
                name="orderAmount"
                value={formData.orderAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="depositAmount">Số tiền đặt cọc (VNĐ)</label>
              <input
                type="number"
                id="depositAmount"
                name="depositAmount"
                value={formData.depositAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="remainingAmount">Số tiền còn lại (VNĐ)</label>
              <input
                type="number"
                id="remainingAmount"
                name="remainingAmount"
                value={formData.remainingAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="PENDING">Chờ xử lý</option>
                <option value="CONFIRMED">Đã xác nhận</option>
                <option value="IN_PRODUCTION">Đang sản xuất</option>
                <option value="READY_FOR_DELIVERY">Sẵn sàng giao hàng</option>
                <option value="DELIVERED">Đã giao hàng</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
            </div>

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleInputChange}
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
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                rows={4}
                placeholder="Ghi chú về đơn hàng..."
              />
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {order.createdAt ? new Date(order.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Tổng giá trị: {formData.orderAmount ? new Intl.NumberFormat('vi-VN').format(formData.orderAmount) + ' VNĐ' : 'N/A'}</span>
              </div>
              <div className="info-item">
                <FileText size={16} />
                <span>Mã đơn hàng: #{order.orderId}</span>
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

export default OrderModal;
