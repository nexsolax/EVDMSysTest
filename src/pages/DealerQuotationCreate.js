import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { 
  FileText, 
  ArrowLeft,
  Plus,
  X,
  Save,
  Package,
  AlertCircle
} from 'lucide-react';
import { 
  dealerQuotationAPI, 
  dealerOrderAPI,
  vehicleAPI 
} from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerQuotationCreate.css';

const DealerQuotationCreate = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [order, setOrder] = useState(null);
  const [quotationItems, setQuotationItems] = useState([]);
  
  // Form data
  const [formData, setFormData] = useState({
    validUntil: '',
    notes: '',
    discountPercentage: 0,
    discountAmount: 0
  });

  const orderId = searchParams.get('orderId');

  useEffect(() => {
    if (orderId) {
      loadOrderData();
    } else {
      toast.error('Vui lòng chọn đơn hàng để tạo báo giá');
      navigate('/admin/dealer-orders');
    }
  }, [orderId]);

  const loadOrderData = async () => {
    try {
      setLoading(true);
      const orderRes = await dealerOrderAPI.getDealerOrder(orderId);
      const orderData = orderRes.data;
      
      // Check if order is approved
      if (orderData.approvalStatus !== 'APPROVED') {
        toast.error('Đơn hàng phải được duyệt trước khi tạo báo giá');
        navigate(`/admin/dealer-orders/${orderId}`);
        return;
      }
      
      setOrder(orderData);
      
      // Initialize quotation items from order items
      if (orderData.items && orderData.items.length > 0) {
        const items = orderData.items.map(item => ({
          orderItemId: item.itemId,
          variantId: item.variant?.variantId || item.variantId,
          colorId: item.color?.colorId || item.colorId,
          quantity: item.quantity,
          unitPrice: item.unitPrice || item.variant?.priceBase || 0,
          discountPercentage: item.discountPercentage || 0,
          notes: item.notes || ''
        }));
        setQuotationItems(items);
      }
    } catch (error) {
      console.error('Error loading order data:', error);
      toast.error('Không thể tải thông tin đơn hàng');
      navigate('/admin/dealer-orders');
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    // Calculate discount amount if discount percentage changes
    if (name === 'discountPercentage') {
      const total = quotationItems.reduce((sum, item) => {
        const subtotal = item.unitPrice * item.quantity;
        return sum + subtotal;
      }, 0);
      const discountAmount = (total * parseFloat(value || 0)) / 100;
      setFormData(prev => ({ ...prev, discountAmount }));
    }
  };

  const handleItemChange = (index, field, value) => {
    const updatedItems = [...quotationItems];
    updatedItems[index] = { ...updatedItems[index], [field]: value };
    
    // Recalculate if price or discount changes
    if (field === 'unitPrice' || field === 'discountPercentage') {
      const item = updatedItems[index];
      const subtotal = parseFloat(item.unitPrice || 0) * item.quantity;
      const itemDiscount = subtotal * (parseFloat(item.discountPercentage || 0) / 100);
      // Note: finalPrice calculation sẽ được backend xử lý
    }
    
    setQuotationItems(updatedItems);
  };

  const handleSubmit = async () => {
    // Validation
    if (quotationItems.length === 0) {
      toast.error('Đơn hàng phải có ít nhất một sản phẩm');
      return;
    }

    try {
      setSubmitting(true);
      
      // Bước 15: Tạo báo giá từ đơn hàng (EVM_STAFF, ADMIN)
      // POST /api/dealer-quotations/from-order/{dealerOrderId}
      // Query params: evmStaffId?, discountPercentage?, notes?
      console.log('Creating quotation from order:', orderId);
      
      // Theo guide line 1324-1330: Query params (tất cả optional): evmStaffId, discountPercentage, notes
      const params = {
        ...(user?.userId ? { evmStaffId: user.userId } : {}), // Optional - ID nhân viên EVM tạo quotation
        ...(formData.discountPercentage && formData.discountPercentage > 0 ? { discountPercentage: formData.discountPercentage } : {}), // Optional - Phần trăm giảm giá
        ...(formData.notes && formData.notes.trim() ? { notes: formData.notes.trim() } : {}) // Optional - Ghi chú
      };
      
      const response = await dealerQuotationAPI.createFromOrder(orderId, params);
      
      // API trả về quotation object hoặc quotationId
      const quotationId = response.data?.quotationId || 
                         response.data?.quotation?.quotationId || 
                         response.data?.id ||
                         response.data?.dealerQuotationId;
      
      if (quotationId) {
        toast.success('Tạo báo giá thành công!');
        // Navigate to detail page để có thể cập nhật validUntil, discount, etc.
        navigate(`/admin/dealer-quotations/${quotationId}`);
      } else {
        // Nếu không có quotationId, reload list
        toast.success('Tạo báo giá thành công!');
        navigate('/admin/dealer-quotations');
      }
    } catch (error) {
      console.error('Error creating quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể tạo báo giá');
    } finally {
      setSubmitting(false);
    }
  };

  const calculateTotals = () => {
    const subtotal = quotationItems.reduce((sum, item) => {
      const itemSubtotal = (item.unitPrice || 0) * item.quantity;
      const itemDiscount = itemSubtotal * ((item.discountPercentage || 0) / 100);
      return sum + (itemSubtotal - itemDiscount);
    }, 0);
    
    const orderDiscount = subtotal * ((formData.discountPercentage || 0) / 100);
    const total = subtotal - orderDiscount - (formData.discountAmount || 0);
    
    return { subtotal, orderDiscount, total };
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!order) {
    return (
      <div className="dealer-quotation-create">
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

  const { subtotal, orderDiscount, total } = calculateTotals();

  return (
    <div className="dealer-quotation-create">
      <div className="page-header">
        <button onClick={() => navigate(`/admin/dealer-orders/${orderId}`)} className="btn-back">
          <ArrowLeft size={18} /> Quay lại đơn hàng
        </button>
        <div className="header-info">
          <div className="page-title">
            <FileText size={24} />
            <h1>Tạo báo giá từ đơn hàng {order.dealerOrderNumber}</h1>
          </div>
        </div>
      </div>

      <div className="quotation-form">
        {/* Order Info */}
        <div className="form-section">
          <h2>Thông tin đơn hàng</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="label">Đại lý:</span>
              <span className="value">{order.dealer?.dealerName || 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="label">Ngày đặt:</span>
              <span className="value">
                {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}
              </span>
            </div>
            <div className="info-item">
              <span className="label">Tổng số lượng:</span>
              <span className="value">{order.totalQuantity || 0} xe</span>
            </div>
          </div>
        </div>

        {/* Quotation Details - Note: Có thể cập nhật sau khi tạo */}
        <div className="form-section">
          <h2>Thông tin báo giá</h2>
          <div className="info-note">
            <p>💡 Báo giá sẽ được tạo tự động từ đơn hàng. Bạn có thể cập nhật thông tin chi tiết sau khi tạo.</p>
          </div>
        </div>

        {/* Quotation Items */}
        <div className="form-section">
          <h2>Danh sách sản phẩm ({quotationItems.length})</h2>
          <div className="items-list">
            {quotationItems.map((item, index) => {
              const variant = order.items?.find(oi => oi.itemId === item.orderItemId)?.variant;
              const color = order.items?.find(oi => oi.itemId === item.orderItemId)?.color;
              const itemSubtotal = (item.unitPrice || 0) * item.quantity;
              const itemDiscount = itemSubtotal * ((item.discountPercentage || 0) / 100);
              const itemFinal = itemSubtotal - itemDiscount;
              
              return (
                <div key={index} className="item-card">
                  <div className="item-header">
                    <span className="item-number">{index + 1}</span>
                    <h3>
                      {variant?.model?.brand?.brandName} {variant?.model?.modelName} {variant?.variantName}
                    </h3>
                  </div>
                  <div className="item-details-grid">
                    <div className="form-group">
                      <label>Màu</label>
                      <input
                        type="text"
                        value={color?.colorName || 'N/A'}
                        className="form-input"
                        disabled
                      />
                    </div>
                    <div className="form-group">
                      <label>Số lượng</label>
                      <input
                        type="number"
                        value={item.quantity}
                        onChange={(e) => handleItemChange(index, 'quantity', parseInt(e.target.value) || 1)}
                        min="1"
                        className="form-input"
                      />
                    </div>
                    <div className="form-group">
                      <label>Đơn giá (VNĐ)</label>
                      <input
                        type="number"
                        value={item.unitPrice}
                        onChange={(e) => handleItemChange(index, 'unitPrice', parseFloat(e.target.value) || 0)}
                        min="0"
                        className="form-input"
                      />
                    </div>
                    <div className="form-group">
                      <label>Giảm giá (%)</label>
                      <input
                        type="number"
                        value={item.discountPercentage}
                        onChange={(e) => handleItemChange(index, 'discountPercentage', parseFloat(e.target.value) || 0)}
                        min="0"
                        max="100"
                        className="form-input"
                      />
                    </div>
                    <div className="form-group">
                      <label>Thành tiền</label>
                      <input
                        type="text"
                        value={`${itemFinal.toLocaleString('vi-VN')} VNĐ`}
                        className="form-input"
                        disabled
                      />
                    </div>
                    <div className="form-group full-width">
                      <label>Ghi chú</label>
                      <input
                        type="text"
                        value={item.notes}
                        onChange={(e) => handleItemChange(index, 'notes', e.target.value)}
                        className="form-input"
                        placeholder="Ghi chú cho sản phẩm..."
                      />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Summary */}
        <div className="form-section summary-section">
          <h2>Tổng kết</h2>
          <div className="summary-grid">
            <div className="summary-item">
              <span className="label">Tổng phụ:</span>
              <span className="value">{subtotal.toLocaleString('vi-VN')} VNĐ</span>
            </div>
            {orderDiscount > 0 && (
              <div className="summary-item">
                <span className="label">Giảm giá đơn hàng ({formData.discountPercentage}%):</span>
                <span className="value discount">-{orderDiscount.toLocaleString('vi-VN')} VNĐ</span>
              </div>
            )}
            {formData.discountAmount > 0 && (
              <div className="summary-item">
                <span className="label">Giảm giá cố định:</span>
                <span className="value discount">-{formData.discountAmount.toLocaleString('vi-VN')} VNĐ</span>
              </div>
            )}
            <div className="summary-item total">
              <span className="label">Tổng cộng:</span>
              <span className="value total-amount">{total.toLocaleString('vi-VN')} VNĐ</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="form-actions">
          <button
            onClick={() => navigate(`/admin/dealer-orders/${orderId}`)}
            className="btn btn-secondary"
          >
            Hủy
          </button>
          <button
            onClick={handleSubmit}
            disabled={submitting}
            className="btn btn-primary"
          >
            {submitting ? 'Đang tạo...' : 'Tạo báo giá'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DealerQuotationCreate;

