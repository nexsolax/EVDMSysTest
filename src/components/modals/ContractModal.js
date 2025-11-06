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
    contractNumber: '', // Theo guide: required, unique
    contractDate: '',
    deliveryDate: '',
    contractValue: '', // Theo guide: required, > 0 (đổi từ contractAmount)
    paymentTerms: '',
    warrantyPeriodMonths: '', // Theo guide: optional, default 24 (đổi từ warrantyPeriod)
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
            contractNumber: contract.contractNumber || '',
            contractDate: contract.contractDate || '',
            deliveryDate: contract.deliveryDate || '',
            contractValue: contract.contractValue || contract.totalAmount || contract.contractAmount || '',
            paymentTerms: contract.paymentTerms || '',
            warrantyPeriodMonths: contract.warrantyPeriodMonths || contract.warrantyPeriod || '',
            status: contract.status || contract.contractStatus || 'draft', // Hỗ trợ cả status và contractStatus (theo FIELD_REFERENCE_GUIDE.md line 752)
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
        contractNumber: contractData.contractNumber || '',
        contractDate: contractData.contractDate || '',
        deliveryDate: contractData.deliveryDate || '',
        contractValue: contractData.contractValue || contractData.totalAmount || contractData.contractAmount || '',
        paymentTerms: contractData.paymentTerms || '',
        warrantyPeriodMonths: contractData.warrantyPeriodMonths || contractData.warrantyPeriod || '',
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
      
      // Required fields (theo FIELD_REFERENCE_GUIDE.md line 745-752)
      const submitData = {
        contractNumber: formData.contractNumber.trim(),
        contractDate: formData.contractDate, // Format: YYYY-MM-DD
        contractValue: parseFloat(formData.contractValue),
        contractStatus: formData.status || 'draft' // contractStatus hoặc status
      };
      
      // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
      if (formData.customerId) {
        submitData.customerId = formData.customerId;
      }
      if (formData.orderId) {
        submitData.orderId = formData.orderId;
      }
      if (formData.vehicleId) {
        submitData.vehicleId = formData.vehicleId;
      }
      if (formData.deliveryDate?.trim()) {
        submitData.deliveryDate = formData.deliveryDate.trim(); // Format: YYYY-MM-DD
      }
      if (formData.paymentTerms?.trim()) {
        submitData.paymentTerms = formData.paymentTerms.trim();
      }
      if (formData.warrantyPeriodMonths) {
        submitData.warrantyPeriodMonths = parseInt(formData.warrantyPeriodMonths, 10);
      }
      if (formData.notes?.trim()) {
        submitData.notes = formData.notes.trim();
      }
      
      await onSave(contract.contractId, submitData);
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
              <label htmlFor="contractNumber">Số hợp đồng *</label>
              <input
                type="text"
                id="contractNumber"
                name="contractNumber"
                value={formData.contractNumber}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                required
                placeholder="HD-2025-001"
              />
            </div>

            <div className="form-group">
              <label htmlFor="contractValue">Giá trị hợp đồng (VNĐ) *</label>
              <input
                type="number"
                id="contractValue"
                name="contractValue"
                value={formData.contractValue}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0.01"
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
              <label htmlFor="warrantyPeriodMonths">Thời gian bảo hành (tháng)</label>
              <input
                type="number"
                id="warrantyPeriodMonths"
                name="warrantyPeriodMonths"
                value={formData.warrantyPeriodMonths}
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
                <span>Giá trị hợp đồng: {formData.contractValue ? new Intl.NumberFormat('vi-VN').format(formData.contractValue) + ' VNĐ' : 'N/A'}</span>
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
