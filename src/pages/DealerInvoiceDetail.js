import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  FileText, 
  ArrowLeft,
  DollarSign,
  CreditCard,
  ShoppingCart,
  AlertCircle,
  Calendar,
  Printer
} from 'lucide-react';
import { 
  dealerInvoiceAPI,
  dealerPaymentAPI,
  dealerOrderAPI
} from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerInvoiceDetail.css';

const DealerInvoiceDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [invoice, setInvoice] = useState(null);
  const [payments, setPayments] = useState([]);
  const [order, setOrder] = useState(null);
  
  // Modal states
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentFormData, setPaymentFormData] = useState({
    paymentAmount: '',
    paymentMethod: 'BANK_TRANSFER',
    paymentDate: new Date().toISOString().split('T')[0],
    referenceNumber: '',
    notes: ''
  });

  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';
  const canProcessPayment = (isDealerManager || isAdmin) && invoice?.status !== 'paid' && invoice?.status !== 'cancelled';

  useEffect(() => {
    if (id) {
      loadInvoiceDetail();
    }
  }, [id]);

  const loadInvoiceDetail = async () => {
    try {
      setLoading(true);
      const [invoiceRes, paymentsRes] = await Promise.all([
        dealerInvoiceAPI.getInvoice(id),
        dealerPaymentAPI.getPaymentsByInvoice(id).catch(() => ({ data: [] }))
      ]);
      
      const invoiceData = invoiceRes.data;
      setInvoice(invoiceData);
      setPayments(paymentsRes.data || []);
      
      // Load order if available
      if (invoiceData.dealerOrderId) {
        try {
          const orderRes = await dealerOrderAPI.getDealerOrder(invoiceData.dealerOrderId);
          setOrder(orderRes.data);
        } catch (err) {
          console.warn('Could not load order:', err);
        }
      }
    } catch (error) {
      console.error('Error loading invoice detail:', error);
      toast.error('Không thể tải thông tin hóa đơn');
      navigate('/admin/dealer-invoices');
    } finally {
      setLoading(false);
    }
  };

  // Bước 18: Thanh toán (DEALER_MANAGER, ADMIN)
  const handleProcessPayment = async () => {
    if (!paymentFormData.paymentAmount || parseFloat(paymentFormData.paymentAmount) <= 0) {
      toast.error('Vui lòng nhập số tiền thanh toán hợp lệ');
      return;
    }
    
    if (!paymentFormData.paymentDate) {
      toast.error('Vui lòng chọn ngày thanh toán');
      return;
    }

    try {
      const requestData = {
        invoiceId: id,
        paymentAmount: parseFloat(paymentFormData.paymentAmount),
        paymentMethod: paymentFormData.paymentMethod,
        paymentDate: paymentFormData.paymentDate,
        ...(paymentFormData.referenceNumber ? { referenceNumber: paymentFormData.referenceNumber.trim() } : {}),
        ...(paymentFormData.notes ? { notes: paymentFormData.notes.trim() } : {})
      };

      await dealerPaymentAPI.processPayment(requestData);
      toast.success('Thanh toán thành công');
      setShowPaymentModal(false);
      setPaymentFormData({
        paymentAmount: '',
        paymentMethod: 'BANK_TRANSFER',
        paymentDate: new Date().toISOString().split('T')[0],
        referenceNumber: '',
        notes: ''
      });
      loadInvoiceDetail();
    } catch (error) {
      console.error('Error processing payment:', error);
      toast.error(error.response?.data?.error || 'Không thể xử lý thanh toán');
    }
  };

  const getStatusBadge = (status) => {
    // DealerInvoice status: lowercase, snake_case
    const statusMap = {
      'issued': { label: 'Đã phát hành', color: 'blue' },
      'partially_paid': { label: 'Đã thanh toán một phần', color: 'yellow' },
      'paid': { label: 'Đã thanh toán đủ', color: 'green' },
      'overdue': { label: 'Quá hạn', color: 'red' },
      'cancelled': { label: 'Đã hủy', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return <span className={`status-badge status-${config.color}`}>{config.label}</span>;
  };

  const getPaymentStatusBadge = (status) => {
    // DealerPayment status: lowercase
    const statusMap = {
      'pending': { label: 'Chờ xử lý', color: 'yellow' },
      'completed': { label: 'Hoàn thành', color: 'green' },
      'failed': { label: 'Thất bại', color: 'red' },
      'refunded': { label: 'Đã hoàn tiền', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return <span className={`status-badge status-${config.color}`}>{config.label}</span>;
  };

  const calculatePaymentSummary = () => {
    const totalPaid = payments
      .filter(p => p.status === 'completed')
      .reduce((sum, p) => sum + (parseFloat(p.paymentAmount) || 0), 0);
    
    const remaining = invoice ? (parseFloat(invoice.totalAmount) || 0) - totalPaid : 0;
    
    return { totalPaid, remaining };
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!invoice) {
    return (
      <div className="dealer-invoice-detail">
        <div className="error-message">
          <AlertCircle size={48} />
          <h2>Không tìm thấy hóa đơn</h2>
          <button onClick={() => navigate('/admin/dealer-invoices')} className="btn btn-primary">
            Quay lại danh sách
          </button>
        </div>
      </div>
    );
  }

  const { totalPaid, remaining } = calculatePaymentSummary();

  return (
    <div className="dealer-invoice-detail">
      {/* Header */}
      <div className="detail-header">
        <button onClick={() => navigate('/admin/dealer-invoices')} className="btn-back">
          <ArrowLeft size={18} /> Quay lại
        </button>
        <div className="header-info">
          <div className="invoice-number">
            <FileText size={24} />
            <h1>{invoice.invoiceNumber || 'N/A'}</h1>
          </div>
          <div className="status-badges">
            {getStatusBadge(invoice.status)}
          </div>
        </div>
      </div>

      {/* Actions Section */}
      <div className="actions-section">
        {/* Bước 18: Thanh toán (DEALER_MANAGER, ADMIN) */}
        {canProcessPayment && (
          <button
            onClick={() => setShowPaymentModal(true)}
            className="btn btn-primary"
          >
            <CreditCard size={18} /> Thanh toán
          </button>
        )}

        <button
          onClick={() => window.print()}
          className="btn btn-secondary"
        >
          <Printer size={18} /> In hóa đơn
        </button>
      </div>

      {/* Invoice Information */}
      <div className="detail-sections">
        {/* Invoice Info */}
        <div className="detail-section">
          <h2>Thông tin hóa đơn</h2>
          <div className="info-grid">
            {order && (
              <>
                <div className="info-item">
                  <span className="label">Đơn hàng:</span>
                  <span className="value">
                    <button
                      onClick={() => navigate(`/admin/dealer-orders/${order.dealerOrderId}`)}
                      className="link-button"
                    >
                      {order.dealerOrderNumber || 'N/A'}
                    </button>
                  </span>
                </div>
                <div className="info-item">
                  <span className="label">Đại lý:</span>
                  <span className="value">
                    {(() => {
                      let dealer = order.dealer;
                      if (typeof dealer === 'string') {
                        try {
                          dealer = JSON.parse(dealer);
                        } catch (e) {
                          dealer = null;
                        }
                      }
                      return dealer?.dealerName || 'N/A';
                    })()}
                  </span>
                </div>
              </>
            )}
            <div className="info-item">
              <span className="label">Ngày phát hành:</span>
              <span className="value">
                {invoice.invoiceDate ? new Date(invoice.invoiceDate).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Hạn thanh toán:</span>
              <span className="value">
                {invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Tổng thành tiền:</span>
              <span className="value total-amount">
                {invoice.totalAmount ? invoice.totalAmount.toLocaleString('vi-VN') : '0'} VNĐ
              </span>
            </div>
            <div className="info-item">
              <span className="label">Đã thanh toán:</span>
              <span className="value">
                {totalPaid.toLocaleString('vi-VN')} VNĐ
              </span>
            </div>
            <div className="info-item">
              <span className="label">Còn lại:</span>
              <span className="value" style={{ color: remaining > 0 ? '#dc2626' : '#059669', fontWeight: '600' }}>
                {remaining.toLocaleString('vi-VN')} VNĐ
              </span>
            </div>
            {invoice.quotationNumber && (
              <div className="info-item">
                <span className="label">Báo giá:</span>
                <span className="value">
                  <button
                    onClick={() => navigate(`/admin/dealer-quotations/${invoice.quotationId}`)}
                    className="link-button"
                  >
                    {invoice.quotationNumber}
                  </button>
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Payments Section */}
        <div className="detail-section">
          <h2>Lịch sử thanh toán ({payments.length})</h2>
          {payments.length === 0 ? (
            <p className="empty-message">Chưa có thanh toán nào</p>
          ) : (
            <div className="payments-list">
              {payments.map((payment) => (
                <div key={payment.paymentId} className="payment-card">
                  <div className="payment-header">
                    <div>
                      <strong>Số thanh toán: {payment.paymentNumber || 'N/A'}</strong>
                      {getPaymentStatusBadge(payment.status)}
                    </div>
                  </div>
                  <div className="payment-info">
                    <span>Số tiền: {payment.paymentAmount?.toLocaleString('vi-VN')} VNĐ</span>
                    <span>Phương thức: {payment.paymentMethod || 'N/A'}</span>
                    <span>Ngày thanh toán: {payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : 'N/A'}</span>
                    {payment.referenceNumber && (
                      <span>Số tham chiếu: {payment.referenceNumber}</span>
                    )}
                  </div>
                  {payment.notes && (
                    <div className="payment-notes">Ghi chú: {payment.notes}</div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Payment Modal */}
      {showPaymentModal && (
        <div className="modal-overlay" onClick={() => setShowPaymentModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Thanh toán hóa đơn</h3>
            <div className="modal-form">
              <div className="form-group">
                <label>Số tiền thanh toán *</label>
                <input
                  type="number"
                  value={paymentFormData.paymentAmount}
                  onChange={(e) => setPaymentFormData(prev => ({ ...prev, paymentAmount: e.target.value }))}
                  className="form-input"
                  placeholder="Nhập số tiền"
                  min="0"
                  max={remaining}
                />
                <small className="form-hint">Còn lại: {remaining.toLocaleString('vi-VN')} VNĐ</small>
              </div>
              
              <div className="form-group">
                <label>Phương thức thanh toán *</label>
                <select
                  value={paymentFormData.paymentMethod}
                  onChange={(e) => setPaymentFormData(prev => ({ ...prev, paymentMethod: e.target.value }))}
                  className="form-select"
                >
                  <option value="BANK_TRANSFER">Chuyển khoản</option>
                  <option value="CASH">Tiền mặt</option>
                  <option value="CREDIT_CARD">Thẻ tín dụng</option>
                  <option value="CHEQUE">Séc</option>
                </select>
              </div>
              
              <div className="form-group">
                <label>Ngày thanh toán *</label>
                <input
                  type="date"
                  value={paymentFormData.paymentDate}
                  onChange={(e) => setPaymentFormData(prev => ({ ...prev, paymentDate: e.target.value }))}
                  className="form-input"
                  max={new Date().toISOString().split('T')[0]}
                />
              </div>
              
              <div className="form-group">
                <label>Số tham chiếu (tùy chọn)</label>
                <input
                  type="text"
                  value={paymentFormData.referenceNumber}
                  onChange={(e) => setPaymentFormData(prev => ({ ...prev, referenceNumber: e.target.value }))}
                  className="form-input"
                  placeholder="Số tham chiếu giao dịch"
                />
              </div>
              
              <div className="form-group">
                <label>Ghi chú (tùy chọn)</label>
                <textarea
                  value={paymentFormData.notes}
                  onChange={(e) => setPaymentFormData(prev => ({ ...prev, notes: e.target.value }))}
                  className="form-textarea"
                  rows="3"
                  placeholder="Nhập ghi chú..."
                />
              </div>
              
              <div className="modal-actions">
                <button
                  onClick={() => setShowPaymentModal(false)}
                  className="btn btn-secondary"
                >
                  Hủy
                </button>
                <button
                  onClick={handleProcessPayment}
                  className="btn btn-primary"
                >
                  <CreditCard size={18} /> Xác nhận thanh toán
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DealerInvoiceDetail;

