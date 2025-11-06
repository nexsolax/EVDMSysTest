import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ShoppingCart, 
  ArrowLeft,
  CheckCircle,
  XCircle,
  FileText,
  DollarSign,
  Truck,
  AlertCircle,
  User,
  Calendar,
  Package,
  Eye
} from 'lucide-react';
import { 
  dealerOrderAPI, 
  dealerQuotationAPI, 
  dealerPaymentAPI,
  dealerInvoiceAPI,
  deliveryAPI
} from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerOrderDetail.css';

const DealerOrderDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [order, setOrder] = useState(null);
  const [quotations, setQuotations] = useState([]);
  const [payments, setPayments] = useState([]);
  const [invoices, setInvoices] = useState([]);
  
  // Modal states
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [showRequestQuotationModal, setShowRequestQuotationModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [requestQuotationNotes, setRequestQuotationNotes] = useState('');

  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';
  const canApprove = isEVMStaff || isAdmin;
  
  // Phân quyền theo guide:
  // - DEALER_MANAGER chỉ có thể thao tác với order/quotation của dealer mình (own)
  // - ADMIN có thể thao tác với tất cả (all)
  const isOrderOwner = isAdmin || (isDealerManager && order && user?.dealerId && order.dealerId === user.dealerId);
  const canRequestQuotation = isOrderOwner && order?.approvalStatus === 'APPROVED'; // Order phải APPROVED

  useEffect(() => {
    if (id) {
      loadOrderDetail();
    }
  }, [id]);

  const loadOrderDetail = async () => {
    try {
      setLoading(true);
      const [orderRes, quotationsRes] = await Promise.all([
        dealerOrderAPI.getDealerOrder(id),
        dealerQuotationAPI.getQuotationsByOrder(id).catch(() => ({ data: [] }))
      ]);
      
      const orderData = orderRes.data;
      setOrder(orderData);
      setQuotations(quotationsRes.data || []);
      
      // Load invoices và payments từ các quotation đã accepted
      // Tìm quotation đã accepted và load invoice của nó
      const acceptedQuotation = (quotationsRes.data || []).find(q => q.status === 'accepted');
      if (acceptedQuotation) {
        try {
          // Load invoices từ order (invoice được tạo tự động khi accept quotation)
          // API: GET /api/dealer-invoices/dealer-order/{dealerOrderId}
          const invoicesRes = await dealerInvoiceAPI.getInvoicesByOrder(id).catch(() => ({ data: [] }));
          const invoicesData = invoicesRes.data || [];
          setInvoices(invoicesData);
          
          // Load payments từ invoices
          if (invoicesData.length > 0) {
            const allPayments = [];
            for (const invoice of invoicesData) {
              try {
                const paymentsRes = await dealerPaymentAPI.getPaymentsByInvoice(invoice.invoiceId);
                if (paymentsRes.data) {
                  allPayments.push(...paymentsRes.data);
                }
              } catch (e) {
                console.warn('Could not load payments for invoice:', invoice.invoiceId);
              }
            }
            setPayments(allPayments);
          }
        } catch (e) {
          console.warn('Could not load invoices:', e);
        }
      } else {
        setInvoices([]);
        setPayments([]);
      }
    } catch (error) {
      console.error('Error loading order detail:', error);
      toast.error('Không thể tải thông tin đơn hàng');
      navigate('/admin/dealer-orders');
    } finally {
      setLoading(false);
    }
  };

  // Bước 13: Duyệt đơn hàng (EVM_STAFF, ADMIN)
  const handleApprove = async () => {
    try {
      await dealerOrderAPI.approveOrder(id, user.userId);
      toast.success('Đã duyệt đơn hàng thành công');
      setShowApproveModal(false);
      loadOrderDetail();
    } catch (error) {
      console.error('Error approving order:', error);
      toast.error(error.response?.data?.error || 'Không thể duyệt đơn hàng');
    }
  };

  // Bước 13: Từ chối đơn hàng (EVM_STAFF, ADMIN)
  const handleReject = async () => {
    if (!rejectReason.trim()) {
      toast.error('Vui lòng nhập lý do từ chối');
      return;
    }
    try {
      await dealerOrderAPI.rejectOrder(id, rejectReason);
      toast.success('Đã từ chối đơn hàng');
      setShowRejectModal(false);
      setRejectReason('');
      loadOrderDetail();
    } catch (error) {
      console.error('Error rejecting order:', error);
      toast.error(error.response?.data?.error || 'Không thể từ chối đơn hàng');
    }
  };

  // Bước 14: Yêu cầu báo giá (DEALER_MANAGER, ADMIN) - Order phải APPROVED
  const handleRequestQuotation = async () => {
    try {
      // Optional field - chỉ gửi nếu có giá trị (không gửi null/undefined/empty)
      const notes = requestQuotationNotes?.trim();
      await dealerOrderAPI.requestQuotation(id, notes || undefined);
      toast.success('Đã gửi yêu cầu báo giá');
      setShowRequestQuotationModal(false);
      setRequestQuotationNotes('');
      loadOrderDetail();
    } catch (error) {
      console.error('Error requesting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể yêu cầu báo giá');
    }
  };

  // Bước 17: Chấp nhận báo giá (DEALER_MANAGER own, ADMIN all)
  // Theo guide: Chỉ DEALER_MANAGER có thể accept quotation của mình
  const handleAcceptQuotation = async (quotationId) => {
    // Kiểm tra phân quyền: Dealer Manager chỉ có thể accept quotation của chính dealer mình
    if (isDealerManager && !isAdmin) {
      const quotation = quotations.find(q => q.quotationId === quotationId);
      if (quotation && order && user?.dealerId && order.dealerId !== user.dealerId) {
        toast.error('Bạn chỉ có thể chấp nhận báo giá của đơn hàng thuộc về đại lý của bạn');
        return;
      }
    }
    
    if (!window.confirm('Bạn có chắc chắn muốn chấp nhận báo giá này? Hệ thống sẽ tự động tạo Invoice.')) {
      return;
    }
    try {
      await dealerQuotationAPI.acceptQuotation(quotationId);
      toast.success('Đã chấp nhận báo giá. Invoice đã được tạo tự động.');
      
      // Reload để lấy invoice mới được tạo
      await loadOrderDetail();
      
      // Hiển thị thông báo với link đến invoice nếu có
      const updatedInvoices = await dealerInvoiceAPI.getInvoicesByOrder(id).catch(() => ({ data: [] }));
      if (updatedInvoices.data && updatedInvoices.data.length > 0) {
        const newInvoice = updatedInvoices.data[0];
        toast.success(
          <div>
            <p>Đã chấp nhận báo giá và tạo Invoice thành công!</p>
            <button 
              onClick={() => navigate(`/admin/dealer-invoices/${newInvoice.invoiceId}`)}
              style={{ marginTop: '8px', padding: '4px 8px' }}
            >
              Xem Invoice {newInvoice.invoiceNumber}
            </button>
          </div>,
          { duration: 5000 }
        );
      }
    } catch (error) {
      console.error('Error accepting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể chấp nhận báo giá');
    }
  };

  // Bước 17: Từ chối báo giá
  const handleRejectQuotation = async (quotationId) => {
    const reason = window.prompt('Nhập lý do từ chối báo giá:');
    if (!reason || !reason.trim()) {
      return;
    }
    try {
      await dealerQuotationAPI.rejectQuotation(quotationId, reason);
      toast.success('Đã từ chối báo giá');
      loadOrderDetail();
    } catch (error) {
      console.error('Error rejecting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể từ chối báo giá');
    }
  };

  // Bước 19: Tạo lịch giao xe (EVM_STAFF, ADMIN)
  const handleCreateDelivery = async () => {
    const scheduledDate = window.prompt('Nhập ngày giao dự kiến (YYYY-MM-DD):', order.expectedDeliveryDate || '');
    if (!scheduledDate || !scheduledDate.trim()) {
      return;
    }
    
    const deliveryAddress = window.prompt('Nhập địa chỉ giao hàng (tùy chọn, Enter để bỏ qua):', order.dealer?.address || '');
    const notes = window.prompt('Nhập ghi chú (tùy chọn, Enter để bỏ qua):', '');
    
    try {
      const requestData = {
        scheduledDeliveryDate: scheduledDate.trim(),
        ...(deliveryAddress && deliveryAddress.trim() ? { deliveryAddress: deliveryAddress.trim() } : {}),
        ...(notes && notes.trim() ? { notes: notes.trim() } : {}),
        ...(user?.userId ? { deliveredBy: user.userId } : {})
      };
      
      await deliveryAPI.createDeliveryFromDealerOrder(id, requestData);
      toast.success('Đã tạo lịch giao xe thành công');
      loadOrderDetail();
    } catch (error) {
      console.error('Error creating delivery:', error);
      toast.error(error.response?.data?.error || 'Không thể tạo lịch giao xe');
    }
  };

  const getStatusBadge = (status) => {
    // DealerOrder status: UPPERCASE (theo ENUM_AND_STATUS_GUIDE.md line 292-305)
    const statusMap = {
      'PENDING': { label: 'Chờ duyệt', color: 'yellow' },
      'APPROVED': { label: 'Đã duyệt', color: 'green' },
      'REJECTED': { label: 'Bị từ chối', color: 'red' },
      'CONFIRMED': { label: 'Đã xác nhận', color: 'blue' },
      'WAITING_FOR_QUOTATION': { label: 'Chờ báo giá', color: 'orange' },
      'IN_PRODUCTION': { label: 'Đang sản xuất', color: 'blue' },
      'READY_FOR_DELIVERY': { label: 'Sẵn sàng giao', color: 'purple' },
      'DELIVERED': { label: 'Đã giao', color: 'green' },
      'CANCELLED': { label: 'Đã hủy', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return <span className={`status-badge status-${config.color}`}>{config.label}</span>;
  };

  const getApprovalBadge = (approvalStatus) => {
    // ApprovalStatus: UPPERCASE (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'PENDING': { label: 'Chờ duyệt', color: 'yellow' },
      'APPROVED': { label: 'Đã duyệt', color: 'green' },
      'REJECTED': { label: 'Bị từ chối', color: 'red' }
    };
    const config = statusMap[approvalStatus] || { label: approvalStatus, color: 'gray' };
    return <span className={`status-badge status-${config.color}`}>{config.label}</span>;
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!order) {
    return (
      <div className="dealer-order-detail">
        <div className="error-message">
          <AlertCircle size={48} />
          <h2>Không tìm thấy đơn hàng</h2>
          <button onClick={() => navigate('/admin/dealer-orders')} className="btn btn-primary">
            Quay lại danh sách
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dealer-order-detail">
      {/* Header */}
      <div className="detail-header">
        <button onClick={() => navigate('/admin/dealer-orders')} className="btn-back">
          <ArrowLeft size={18} /> Quay lại
        </button>
        <div className="header-info">
          <div className="order-number">
            <ShoppingCart size={24} />
            <h1>{order.dealerOrderNumber || 'N/A'}</h1>
          </div>
          <div className="status-badges">
            {getStatusBadge(order.status)}
            {getApprovalBadge(order.approvalStatus)}
          </div>
        </div>
      </div>

      {/* Actions Section - Theo role và status */}
      <div className="actions-section">
        {/* Bước 13: Duyệt/Từ chối đơn hàng (EVM_STAFF, ADMIN) */}
        {canApprove && order.approvalStatus === 'PENDING' && (
          <>
            <button
              onClick={() => setShowApproveModal(true)}
              className="btn btn-success"
            >
              <CheckCircle size={18} /> Duyệt đơn hàng
            </button>
            <button
              onClick={() => setShowRejectModal(true)}
              className="btn btn-danger"
            >
              <XCircle size={18} /> Từ chối đơn hàng
            </button>
          </>
        )}

        {/* Bước 14: Yêu cầu báo giá (DEALER_MANAGER, ADMIN) - Order phải APPROVED */}
        {canRequestQuotation && (
          <button
            onClick={() => setShowRequestQuotationModal(true)}
            className="btn btn-primary"
          >
            <FileText size={18} /> Yêu cầu báo giá
          </button>
        )}

        {/* Bước 15: Tạo báo giá (EVM_STAFF, ADMIN) - Chỉ hiện khi có request */}
        {isEVMStaff && order.approvalStatus === 'APPROVED' && (
          <button
            onClick={() => navigate(`/admin/dealer-quotations/create?orderId=${id}`)}
            className="btn btn-primary"
          >
            <FileText size={18} /> Tạo báo giá
          </button>
        )}

        {/* Bước 19: Tạo lịch giao xe (EVM_STAFF, ADMIN) - Order phải APPROVED */}
        {isEVMStaff && order.approvalStatus === 'APPROVED' && (
          <button
            onClick={handleCreateDelivery}
            className="btn btn-primary"
          >
            <Truck size={18} /> Tạo lịch giao xe
          </button>
        )}
      </div>

      {/* Order Information */}
      <div className="detail-sections">
        <div className="detail-section">
          <h2>Thông tin đơn hàng</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="label">Đại lý:</span>
              <span className="value">{order.dealer?.dealerName || 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="label">Mã đại lý:</span>
              <span className="value">{order.dealer?.dealerCode || 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="label">Ngày đặt hàng:</span>
              <span className="value">
                {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Ngày giao dự kiến:</span>
              <span className="value">
                {order.expectedDeliveryDate ? new Date(order.expectedDeliveryDate).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Loại đơn hàng:</span>
              <span className="value">
                {order.orderType === 'PURCHASE' ? 'Mua hàng' : 
                 order.orderType === 'RESERVE' ? 'Đặt trước' : 'Mẫu'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Độ ưu tiên:</span>
              <span className="value">
                {order.priority === 'LOW' ? 'Thấp' :
                 order.priority === 'NORMAL' ? 'Bình thường' :
                 order.priority === 'HIGH' ? 'Cao' : 'Khẩn cấp'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Tổng số lượng:</span>
              <span className="value">{order.totalQuantity || 0} xe</span>
            </div>
            <div className="info-item">
              <span className="label">Tổng thành tiền:</span>
              <span className="value total-amount">
                {order.totalAmount ? order.totalAmount.toLocaleString('vi-VN') : '0'} VNĐ
              </span>
            </div>
            {order.paymentTerms && (
              <div className="info-item">
                <span className="label">Điều khoản thanh toán:</span>
                <span className="value">{order.paymentTerms}</span>
              </div>
            )}
            {order.deliveryTerms && (
              <div className="info-item">
                <span className="label">Điều khoản giao hàng:</span>
                <span className="value">{order.deliveryTerms}</span>
              </div>
            )}
            {order.notes && (
              <div className="info-item full-width">
                <span className="label">Ghi chú:</span>
                <span className="value">{order.notes}</span>
              </div>
            )}
          </div>
        </div>

        {/* Order Items */}
        {order.items && order.items.length > 0 && (
          <div className="detail-section">
            <h2>Danh sách xe ({order.items.length})</h2>
            <div className="items-list">
              {order.items.map((item, index) => (
                <div key={item.itemId || index} className="item-card">
                  <div className="item-header">
                    <span className="item-number">{index + 1}</span>
                    <h3>
                      {item.variant?.model?.brand?.brandName} {item.variant?.model?.modelName} {item.variant?.variantName}
                    </h3>
                  </div>
                  <div className="item-details">
                    <span>Màu: {item.color?.colorName || 'N/A'}</span>
                    <span>Số lượng: {item.quantity}</span>
                    <span>Đơn giá: {item.unitPrice?.toLocaleString('vi-VN')} VNĐ</span>
                    {item.discountPercentage && (
                      <span>Giảm giá: {item.discountPercentage}%</span>
                    )}
                    <span className="item-total">
                      Thành tiền: {item.finalPrice?.toLocaleString('vi-VN')} VNĐ
                    </span>
                  </div>
                  {item.notes && (
                    <div className="item-notes">Ghi chú: {item.notes}</div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Quotations Section */}
        <div className="detail-section">
          <h2>Báo giá ({quotations.length})</h2>
          {quotations.length === 0 ? (
            <p className="empty-message">Chưa có báo giá</p>
          ) : (
            <div className="quotations-list">
              {quotations.map((quotation) => (
                <div key={quotation.quotationId} className="quotation-card">
                  <div className="quotation-header">
                    <div>
                      <strong>Số báo giá: {quotation.quotationNumber || 'N/A'}</strong>
                      <span className={`status-badge status-${
                        quotation.status === 'sent' ? 'blue' : 
                        quotation.status === 'accepted' ? 'green' : 
                        quotation.status === 'rejected' ? 'red' :
                        quotation.status === 'pending' ? 'gray' :
                        quotation.status === 'expired' ? 'yellow' : 'gray'
                      }`}>
                        {quotation.status === 'sent' ? 'Đã gửi' : 
                         quotation.status === 'accepted' ? 'Đã chấp nhận' : 
                         quotation.status === 'rejected' ? 'Đã từ chối' : 
                         quotation.status === 'pending' ? 'Nháp' :
                         quotation.status === 'expired' ? 'Hết hạn' :
                         quotation.status === 'converted' ? 'Đã chuyển đổi' : quotation.status}
                      </span>
                    </div>
                    <div className="quotation-actions">
                      {/* Bước 17: Accept/Reject - chỉ khi status = sent (lowercase) */}
                      {/* DEALER_MANAGER chỉ có thể accept quotation của chính dealer mình (own) */}
                      {isOrderOwner && quotation.status === 'sent' && (
                        <>
                          <button
                            onClick={() => handleAcceptQuotation(quotation.quotationId)}
                            className="btn btn-success btn-sm"
                          >
                            <CheckCircle size={16} /> Chấp nhận
                          </button>
                          <button
                            onClick={() => handleRejectQuotation(quotation.quotationId)}
                            className="btn btn-danger btn-sm"
                          >
                            <XCircle size={16} /> Từ chối
                          </button>
                        </>
                      )}
                      <button
                        onClick={() => navigate(`/admin/dealer-quotations/${quotation.quotationId}`)}
                        className="btn btn-primary btn-sm"
                      >
                        <Eye size={16} /> Xem chi tiết
                      </button>
                    </div>
                  </div>
                  <div className="quotation-info">
                    <span>Tổng tiền: {quotation.totalAmount?.toLocaleString('vi-VN')} VNĐ</span>
                    <span>Ngày tạo: {quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Invoices Section - Hiển thị sau khi accept quotation */}
        {invoices.length > 0 && (
          <div className="detail-section">
            <h2>Hóa đơn ({invoices.length})</h2>
            <div className="invoices-list">
              {invoices.map((invoice) => (
                <div key={invoice.invoiceId} className="invoice-card">
                  <div className="invoice-header">
                    <div>
                      <strong>Số hóa đơn: {invoice.invoiceNumber || 'N/A'}</strong>
                      <span className={`status-badge status-${
                        invoice.status === 'paid' ? 'green' : 
                        invoice.status === 'partially_paid' ? 'yellow' :
                        invoice.status === 'issued' ? 'blue' :
                        invoice.status === 'overdue' ? 'red' :
                        'gray'
                      }`}>
                        {invoice.status === 'paid' ? 'Đã thanh toán đủ' : 
                         invoice.status === 'partially_paid' ? 'Đã thanh toán một phần' :
                         invoice.status === 'issued' ? 'Đã phát hành' :
                         invoice.status === 'overdue' ? 'Quá hạn' :
                         invoice.status === 'cancelled' ? 'Đã hủy' : invoice.status}
                      </span>
                    </div>
                    <button
                      onClick={() => navigate(`/admin/dealer-invoices/${invoice.invoiceId}`)}
                      className="btn btn-primary btn-sm"
                    >
                      <Eye size={16} /> Xem hóa đơn
                    </button>
                  </div>
                  <div className="invoice-info">
                    <span>Tổng tiền: {invoice.totalAmount?.toLocaleString('vi-VN')} VNĐ</span>
                    <span>Ngày phát hành: {invoice.invoiceDate ? new Date(invoice.invoiceDate).toLocaleDateString('vi-VN') : 'N/A'}</span>
                    {invoice.dueDate && (
                      <span>Hạn thanh toán: {new Date(invoice.dueDate).toLocaleDateString('vi-VN')}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Payments Section */}
        {payments.length > 0 && (
          <div className="detail-section">
            <h2>Thanh toán ({payments.length})</h2>
            <div className="payments-list">
              {payments.map((payment) => (
                <div key={payment.paymentId} className="payment-card">
                  <div className="payment-header">
                    <strong>Số thanh toán: {payment.paymentNumber || 'N/A'}</strong>
                    <span className={`status-badge status-${
                      payment.status === 'completed' ? 'green' : 
                      payment.status === 'pending' ? 'yellow' :
                      payment.status === 'failed' ? 'red' :
                      payment.status === 'refunded' ? 'gray' : 'yellow'
                    }`}>
                      {payment.status === 'completed' ? 'Hoàn tất' : 
                       payment.status === 'pending' ? 'Chờ xử lý' :
                       payment.status === 'failed' ? 'Thất bại' :
                       payment.status === 'refunded' ? 'Đã hoàn tiền' : payment.status}
                    </span>
                  </div>
                  <div className="payment-info">
                    <span>Số tiền: {payment.amount?.toLocaleString('vi-VN')} VNĐ</span>
                    <span>Ngày thanh toán: {payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : 'N/A'}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Approve Modal */}
      {showApproveModal && (
        <div className="modal-overlay" onClick={() => setShowApproveModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Xác nhận duyệt đơn hàng</h3>
            <p>Bạn có chắc chắn muốn duyệt đơn hàng này?</p>
            <div className="modal-actions">
              <button onClick={() => setShowApproveModal(false)} className="btn btn-secondary">
                Hủy
              </button>
              <button onClick={handleApprove} className="btn btn-success">
                Duyệt
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Từ chối đơn hàng</h3>
            <label>Lý do từ chối *</label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              className="form-textarea"
              rows="4"
              placeholder="Nhập lý do từ chối..."
            />
            <div className="modal-actions">
              <button onClick={() => {
                setShowRejectModal(false);
                setRejectReason('');
              }} className="btn btn-secondary">
                Hủy
              </button>
              <button onClick={handleReject} className="btn btn-danger">
                Từ chối
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Request Quotation Modal */}
      {showRequestQuotationModal && (
        <div className="modal-overlay" onClick={() => setShowRequestQuotationModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Yêu cầu báo giá</h3>
            <label>Ghi chú (tùy chọn)</label>
            <textarea
              value={requestQuotationNotes}
              onChange={(e) => setRequestQuotationNotes(e.target.value)}
              className="form-textarea"
              rows="4"
              placeholder="Nhập ghi chú cho yêu cầu báo giá..."
            />
            <div className="modal-actions">
              <button onClick={() => {
                setShowRequestQuotationModal(false);
                setRequestQuotationNotes('');
              }} className="btn btn-secondary">
                Hủy
              </button>
              <button onClick={handleRequestQuotation} className="btn btn-primary">
                Gửi yêu cầu
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DealerOrderDetail;

