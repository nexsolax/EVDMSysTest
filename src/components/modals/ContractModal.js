import React, { useState, useEffect } from 'react';
import { X, Save, DollarSign, Calendar, FileText } from 'lucide-react';
import { contractAPI, customerAPI, inventoryAPI, orderAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const ContractModal = ({ contract, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    orderId: '',
    vehicleId: '',
    contractDate: '',
    deliveryDate: '',
    contractAmount: '',
    paymentTerms: '',
    warrantyPeriod: '',
    status: 'draft',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [orders, setOrders] = useState([]);

useEffect(() => {
    if (isOpen) {
      loadCustomers();
      loadVehicles();
      loadOrders();
      if (contract) {
        if (mode === 'edit') {
        loadContractDetails();
        } else {
          setFormData({
            customerId: contract.customer?.customerId || contract.customerId || '',
            orderId: contract.order?.orderId || contract.orderId || '',
            vehicleId: contract.vehicle?.vehicleId || contract.vehicleId || '',
            contractDate: contract.contractDate || '',
            deliveryDate: contract.deliveryDate || '',
            contractAmount: contract.totalAmount || contract.contractAmount || '',
            paymentTerms: contract.paymentTerms || '',
            warrantyPeriod: contract.warrantyPeriod || '',
            status: contract.status || 'draft',
            notes: contract.notes || ''
          });
        }
      }
    }
// eslint-disable-next-line react-hooks/exhaustive-deps
}, [isOpen, contract, mode]);

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
      // Load inventory items instead of vehicles
      const response = await inventoryAPI.getInventory();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading inventory:', error);
    }
  };

  const loadOrders = async () => {
    try {
      const response = await orderAPI.getActiveOrders();
      setOrders(response.data || []);
    } catch (error) {
      console.error('Error loading orders:', error);
    }
  };

  const loadContractDetails = async () => {
    try {
      setLoading(true);
      const response = await contractAPI.getContract(contract.contractId);
      const contractData = response.data;
      setFormData({
        customerId: contractData.customer?.customerId || contractData.customerId || '',
        orderId: contractData.order?.orderId || contractData.orderId || '',
        vehicleId: contractData.vehicle?.vehicleId || contractData.vehicleId || '',
        contractDate: contractData.contractDate || '',
        deliveryDate: contractData.deliveryDate || '',
        contractAmount: contractData.totalAmount || contractData.contractAmount || '',
        paymentTerms: contractData.paymentTerms || '',
        warrantyPeriod: contractData.warrantyPeriod || '',
        status: contractData.status || 'draft',
        notes: contractData.notes || ''
      });
    } catch (error) {
      console.error('Error loading contract details:', error);
      toast.error('Không thể tải thông tin hợp đồng');
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
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(contract.contractId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving contract:', error);
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
            <FileText size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết hợp đồng' : 'Chỉnh sửa hợp đồng'}</h2>
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
              <label htmlFor="orderId">Đơn hàng</label>
              <select
                id="orderId"
                name="orderId"
                value={formData.orderId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn đơn hàng</option>
                {orders.map(order => (
                  <option key={order.orderId} value={order.orderId}>
                    #{order.orderId} - {order.customer?.firstName} {order.customer?.lastName}
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
              <label htmlFor="contractDate">Ngày ký hợp đồng</label>
              <input
                type="date"
                id="contractDate"
                name="contractDate"
                value={formData.contractDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="deliveryDate">Ngày giao xe</label>
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
              <label htmlFor="contractAmount">Giá trị hợp đồng (VNĐ) - Lấy từ Order</label>
              <input
                type="number"
                id="contractAmount"
                name="contractAmount"
                value={formData.contractAmount}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="paymentTerms">Điều khoản thanh toán</label>
              <select
                id="paymentTerms"
                name="paymentTerms"
                value={formData.paymentTerms}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn điều khoản thanh toán</option>
                <option value="FULL_PAYMENT">Thanh toán toàn bộ</option>
                <option value="INSTALLMENT_12">Trả góp 12 tháng</option>
                <option value="INSTALLMENT_24">Trả góp 24 tháng</option>
                <option value="INSTALLMENT_36">Trả góp 36 tháng</option>
                <option value="INSTALLMENT_48">Trả góp 48 tháng</option>
                <option value="INSTALLMENT_60">Trả góp 60 tháng</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="warrantyPeriod">Thời gian bảo hành (tháng)</label>
              <input
                type="number"
                id="warrantyPeriod"
                name="warrantyPeriod"
                value={formData.warrantyPeriod}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                max="120"
                placeholder="24"
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
                <option value="draft">Nháp</option>
                <option value="pending">Chờ xử lý</option>
                <option value="signed">Đã ký</option>
                <option value="active">Có hiệu lực</option>
                <option value="completed">Hoàn thành</option>
                <option value="cancelled">Đã hủy</option>
                <option value="expired">Hết hạn</option>
              </select>
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
                placeholder="Ghi chú về hợp đồng..."
              />
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {contract.createdAt ? new Date(contract.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Giá trị hợp đồng: {formData.contractAmount ? new Intl.NumberFormat('vi-VN').format(formData.contractAmount) + ' VNĐ' : 'N/A'}</span>
              </div>
              <div className="info-item">
                <FileText size={16} />
                <span>Mã hợp đồng: #{contract.contractId}</span>
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

export default ContractModal;
