import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  FileText, 
  ArrowLeft,
  Send,
  CheckCircle,
  XCircle,
  Eye,
  AlertCircle,
  Printer,
  Edit
} from 'lucide-react';
import { 
  dealerQuotationAPI,
  dealerOrderAPI
} from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerQuotationDetail.css';

const DealerQuotationDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [quotation, setQuotation] = useState(null);
  const [quotationItems, setQuotationItems] = useState([]);
  const [order, setOrder] = useState(null);
  
  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';
  
  // Phân quyền theo guide:
  // - DEALER_MANAGER chỉ có thể accept quotation của dealer mình (own)
  // - ADMIN có thể accept tất cả (all)
  const isQuotationOwner = isAdmin || (isDealerManager && order && user?.dealerId && order.dealerId === user.dealerId);

  useEffect(() => {
    if (id) {
      loadQuotationDetail();
    }
  }, [id]);

  const loadQuotationDetail = async () => {
    try {
      setLoading(true);
      const [quotationRes, itemsRes] = await Promise.all([
        dealerQuotationAPI.getQuotation(id),
        dealerQuotationAPI.getQuotationItems(id).catch(() => ({ data: [] }))
      ]);
      
      const quotationData = quotationRes.data;
      setQuotation(quotationData);
      setQuotationItems(itemsRes.data || []);
      
      // Load order if available
      if (quotationData.dealerOrderId) {
        try {
          const orderRes = await dealerOrderAPI.getDealerOrder(quotationData.dealerOrderId);
          setOrder(orderRes.data);
        } catch (err) {
          console.warn('Could not load order:', err);
        }
      }
    } catch (error) {
      console.error('Error loading quotation detail:', error);
      toast.error('Không thể tải thông tin báo giá');
      navigate('/admin/dealer-quotations');
    } finally {
      setLoading(false);
    }
  };

  // Bước 16: Gửi báo giá (EVM_STAFF, ADMIN)
  const handleSendQuotation = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn gửi báo giá này cho đại lý?')) {
      return;
    }
    try {
      await dealerQuotationAPI.sendQuotation(id);
      toast.success('Đã gửi báo giá thành công');
      loadQuotationDetail();
    } catch (error) {
      console.error('Error sending quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể gửi báo giá');
    }
  };

  // Bước 17: Chấp nhận báo giá (DEALER_MANAGER own, ADMIN all) - Tự động tạo Invoice
  const handleAcceptQuotation = async () => {
    // Kiểm tra phân quyền: Dealer Manager chỉ có thể accept quotation của chính dealer mình
    if (isDealerManager && !isAdmin) {
      if (!isQuotationOwner) {
        toast.error('Bạn chỉ có thể chấp nhận báo giá của đơn hàng thuộc về đại lý của bạn');
        return;
      }
    }
    
    if (!window.confirm('Bạn có chắc chắn muốn chấp nhận báo giá này? Hệ thống sẽ tự động tạo Invoice.')) {
      return;
    }
    try {
      await dealerQuotationAPI.acceptQuotation(id);
      toast.success('Đã chấp nhận báo giá. Invoice đã được tạo tự động.');
      
      // Reload để lấy thông tin mới (order status sẽ chuyển thành CONFIRMED)
      await loadQuotationDetail();
    } catch (error) {
      console.error('Error accepting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể chấp nhận báo giá');
    }
  };

  // Bước 17: Từ chối báo giá
  const handleRejectQuotation = async () => {
    const reason = window.prompt('Nhập lý do từ chối báo giá:');
    if (!reason || !reason.trim()) {
      return;
    }
    try {
      await dealerQuotationAPI.rejectQuotation(id, reason);
      toast.success('Đã từ chối báo giá');
      loadQuotationDetail();
    } catch (error) {
      console.error('Error rejecting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể từ chối báo giá');
    }
  };

  // Chỉnh sửa status (EVM_STAFF, ADMIN)
  const handleUpdateStatus = async (newStatus) => {
    const statusLabels = {
      'pending': 'Nháp',
      'sent': 'Đã gửi',
      'accepted': 'Đã chấp nhận',
      'rejected': 'Đã từ chối',
      'expired': 'Hết hạn',
      'converted': 'Đã chuyển đổi'
    };
    const statusLabel = statusLabels[newStatus] || newStatus;
    if (!window.confirm(`Bạn có chắc chắn muốn đổi trạng thái báo giá thành "${statusLabel}"?`)) {
      return;
    }
    try {
      await dealerQuotationAPI.updateQuotationStatus(id, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadQuotationDetail();
    } catch (error) {
      console.error('Error updating status:', error);
      toast.error(error.response?.data?.error || 'Không thể cập nhật trạng thái');
    }
  };

  const getStatusBadge = (status) => {
    // DealerQuotation status: lowercase (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'pending': { label: 'Nháp', color: 'gray' },
      'sent': { label: 'Đã gửi', color: 'blue' },
      'accepted': { label: 'Đã chấp nhận', color: 'green' },
      'rejected': { label: 'Đã từ chối', color: 'red' },
      'expired': { label: 'Hết hạn', color: 'yellow' },
      'converted': { label: 'Đã chuyển đổi', color: 'purple' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return <span className={`status-badge status-${config.color}`}>{config.label}</span>;
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!quotation) {
    return (
      <div className="dealer-quotation-detail">
        <div className="error-message">
          <AlertCircle size={48} />
          <h2>Không tìm thấy báo giá</h2>
          <button onClick={() => navigate('/admin/dealer-quotations')} className="btn btn-primary">
            Quay lại danh sách
          </button>
        </div>
      </div>
    );
  }

  // Status values: lowercase (theo DEALER_ORDER_API_FOR_FRONTEND.md)
  const canSend = isEVMStaff && quotation?.status === 'pending';
  const canAccept = isQuotationOwner && quotation?.status === 'sent'; // DEALER_MANAGER own, ADMIN all
  const canReject = isQuotationOwner && quotation?.status === 'sent'; // DEALER_MANAGER own, ADMIN all

  return (
    <div className="dealer-quotation-detail">
      {/* Header */}
      <div className="detail-header">
        <button onClick={() => navigate('/admin/dealer-quotations')} className="btn-back">
          <ArrowLeft size={18} /> Quay lại
        </button>
        <div className="header-info">
          <div className="quotation-number">
            <FileText size={24} />
            <h1>{quotation.quotationNumber || 'N/A'}</h1>
          </div>
          <div className="status-badges">
            {getStatusBadge(quotation.status)}
          </div>
        </div>
      </div>

      {/* Actions Section - Theo role và status */}
      <div className="actions-section">
        {/* Chỉnh sửa status - chỉ EVM_STAFF/ADMIN */}
        {isEVMStaff && (
          <div className="status-edit-group">
            <label>Trạng thái:</label>
            <select
              value={quotation.status}
              onChange={(e) => handleUpdateStatus(e.target.value)}
              className="status-select"
            >
              <option value="pending">Nháp</option>
              <option value="sent">Đã gửi</option>
              <option value="accepted">Đã chấp nhận</option>
              <option value="rejected">Đã từ chối</option>
              <option value="expired">Hết hạn</option>
              <option value="converted">Đã chuyển đổi</option>
            </select>
          </div>
        )}

        {/* Bước 16: Gửi báo giá (EVM_STAFF, ADMIN) - Chỉ khi status = DRAFT */}
        {canSend && (
          <button
            onClick={handleSendQuotation}
            className="btn btn-primary"
          >
            <Send size={18} /> Gửi báo giá
          </button>
        )}

        {/* Bước 17: Chấp nhận/Từ chối báo giá (DEALER_MANAGER, ADMIN) - Chỉ khi status = SENT */}
        {canAccept && (
          <button
            onClick={handleAcceptQuotation}
            className="btn btn-success"
          >
            <CheckCircle size={18} /> Chấp nhận báo giá
          </button>
        )}

        {canReject && (
          <button
            onClick={handleRejectQuotation}
            className="btn btn-danger"
          >
            <XCircle size={18} /> Từ chối báo giá
          </button>
        )}

        <button
          onClick={() => window.print()}
          className="btn btn-secondary"
        >
          <Printer size={18} /> In báo giá
        </button>
      </div>

      {/* Quotation Information */}
      <div className="detail-sections">
        {/* Quotation Info */}
        <div className="detail-section">
          <h2>Thông tin báo giá</h2>
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
                  <span className="value">{order.dealer?.dealerName || 'N/A'}</span>
                </div>
              </>
            )}
            <div className="info-item">
              <span className="label">Ngày tạo:</span>
              <span className="value">
                {quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Ngày hết hạn:</span>
              <span className="value">
                {quotation.validUntil ? new Date(quotation.validUntil).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            {quotation.discountPercentage && (
              <div className="info-item">
                <span className="label">Giảm giá đơn hàng:</span>
                <span className="value">{quotation.discountPercentage}%</span>
              </div>
            )}
            {quotation.discountAmount && (
              <div className="info-item">
                <span className="label">Giảm giá cố định:</span>
                <span className="value">{quotation.discountAmount.toLocaleString('vi-VN')} VNĐ</span>
              </div>
            )}
            <div className="info-item">
              <span className="label">Tổng thành tiền:</span>
              <span className="value total-amount">
                {quotation.totalAmount ? quotation.totalAmount.toLocaleString('vi-VN') : '0'} VNĐ
              </span>
            </div>
            {quotation.notes && (
              <div className="info-item full-width">
                <span className="label">Ghi chú:</span>
                <span className="value">{quotation.notes}</span>
              </div>
            )}
            {quotation.rejectionReason && (
              <div className="info-item full-width">
                <span className="label">Lý do từ chối:</span>
                <span className="value rejection-reason">{quotation.rejectionReason}</span>
              </div>
            )}
          </div>
        </div>

        {/* Quotation Items */}
        {quotationItems.length > 0 && (
          <div className="detail-section">
            <h2>Danh sách sản phẩm ({quotationItems.length})</h2>
            <div className="items-list">
              {quotationItems.map((item, index) => (
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
                    {item.discountAmount && (
                      <span>Giảm giá: -{item.discountAmount.toLocaleString('vi-VN')} VNĐ</span>
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
      </div>
    </div>
  );
};

export default DealerQuotationDetail;

