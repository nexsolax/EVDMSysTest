import React, { useEffect, useState } from 'react';
import { X, Save, CreditCard, DollarSign, Calendar, FileText } from 'lucide-react';
import { paymentAPI, customerAPI, orderAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const PaymentModal = ({ payment, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    orderId: '',
    paymentDate: '',
    amount: '',
    method: '',
    status: 'PENDING',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    if (!isOpen) return;

    const init = async () => {
      await Promise.all([loadCustomers(), loadOrders()]);
      if (payment) {
        if (mode === 'edit') {
          await loadPaymentDetails();
        } else {
          setFormData({
            customerId: payment.customer?.customerId || payment.customerId || '',
            orderId: payment.order?.orderId || payment.orderId || '',
            paymentDate: payment.paymentDate || '',
            amount: payment.amount || '',
            method: payment.method || '',
            status: payment.status || 'PENDING',
            notes: payment.notes || ''
          });
        }
      }
    };

    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, payment, mode]);

  const loadCustomers = async () => {
    try {
      const res = await customerAPI.getActiveCustomers?.();
      setCustomers(res?.data || []);
    } catch {}
  };

  const loadOrders = async () => {
    try {
      const res = await orderAPI.getActiveOrders?.();
      setOrders(res?.data || []);
    } catch {}
  };

  const loadPaymentDetails = async () => {
    try {
      setLoading(true);
      const res = await paymentAPI.getPayment(payment.paymentId);
      const p = res.data;
      setFormData({
        customerId: p.customer?.customerId || p.customerId || '',
        orderId: p.order?.orderId || p.orderId || '',
        paymentDate: p.paymentDate || '',
        amount: p.amount || '',
        method: p.method || '',
        status: p.status || 'PENDING',
        notes: p.notes || ''
      });
    } catch (e) {
      console.error('Error loading payment:', e);
      toast.error('Không thể tải thông tin thanh toán');
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
      await onSave(payment.paymentId, formData);
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
            <CreditCard size={24} />
            <h2>{mode === 'view' ? 'Xem chi tiết thanh toán' : 'Chỉnh sửa thanh toán'}</h2>
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
              <label htmlFor="paymentDate">Ngày thanh toán</label>
              <input
                type="date"
                id="paymentDate"
                name="paymentDate"
                value={formData.paymentDate}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="amount">Số tiền (VNĐ)</label>
              <input
                type="number"
                id="amount"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-input"
                min="0"
                step="1000000"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="method">Phương thức</label>
              <select
                id="method"
                name="method"
                value={formData.method}
                onChange={handleChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="CASH">Tiền mặt</option>
                <option value="BANK_TRANSFER">Chuyển khoản</option>
                <option value="CREDIT_CARD">Thẻ tín dụng</option>
                <option value="E_WALLET">Ví điện tử</option>
              </select>
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
                <option value="PENDING">Chờ xử lý</option>
                <option value="PROCESSED">Đã xử lý</option>
                <option value="REFUNDED">Đã hoàn tiền</option>
                <option value="FAILED">Thất bại</option>
              </select>
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
                placeholder="Ghi chú về thanh toán..."
              />
            </div>
          </div>

          {mode === 'view' && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>
                  Ngày thanh toán: {payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : 'N/A'}
                </span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Số tiền: {formData.amount ? new Intl.NumberFormat('vi-VN').format(formData.amount) + ' VNĐ' : 'N/A'}</span>
              </div>
              <div className="info-item">
                <FileText size={16} />
                <span>Mã thanh toán: #{payment.paymentId}</span>
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

export default PaymentModal;
