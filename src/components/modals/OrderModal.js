import React, { useState, useEffect } from 'react';
import { X, Save, ShoppingCart, DollarSign, Calendar, FileText } from 'lucide-react';
import { orderAPI, customerAPI, vehicleAPI, quotationAPI, inventoryAPI, paymentAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const OrderModal = ({ order, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    customerId: '',
    quotationId: '',
    inventoryId: '',
    orderDate: '',
    deliveryDate: '',
    totalAmount: '',
    depositAmount: '',
    balanceAmount: '',
    status: 'pending',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [quotations, setQuotations] = useState([]);

  // Helper function to format date for input (YYYY-MM-DD)
  const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    try {
      // If it's already in YYYY-MM-DD format, return as is
      if (typeof dateString === 'string' && /^\d{4}-\d{2}-\d{2}/.test(dateString)) {
        return dateString.split('T')[0]; // Take only date part if it's datetime
      }
      // Otherwise, try to parse and format
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      return date.toISOString().split('T')[0];
    } catch (error) {
      console.warn('Error formatting date:', dateString, error);
      return '';
    }
  };

  useEffect(() => {
    if (isOpen) {
      loadCustomers();
      loadVehicles();
      loadQuotations();
      if (order && order.orderId) {
        // Luôn load chi tiết order để có đầy đủ relationships và dữ liệu
        // Vì dữ liệu từ list có thể thiếu relationships
        loadOrderDetails();
      } else if (order) {
        // Nếu không có orderId, dùng dữ liệu hiện có (trường hợp create)
        const totalAmount = order.totalAmount || order.orderAmount || order.amount || 0;
        const depositAmount = order.depositAmount || 0;
        const balanceAmount = order.balanceAmount || order.remainingAmount || (totalAmount - depositAmount);
        
        setFormData({
          customerId: order.customer?.customerId || order.customerId ? String(order.customer?.customerId || order.customerId) : '',
          quotationId: order.quotation?.quotationId || order.quotationId ? String(order.quotation?.quotationId || order.quotationId) : '',
          inventoryId: order.inventory?.inventoryId || order.inventoryId ? String(order.inventory?.inventoryId || order.inventoryId) : '',
          orderDate: formatDateForInput(order.orderDate) || '',
          deliveryDate: formatDateForInput(order.deliveryDate || order.expectedDeliveryDate) || '',
          totalAmount: totalAmount ? String(totalAmount) : '',
          depositAmount: depositAmount ? String(depositAmount) : '',
          balanceAmount: balanceAmount ? String(balanceAmount) : '',
          status: order.status || 'pending',
          notes: order.notes || ''
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, order, mode]);

  const loadCustomers = async () => {
    try {
      const response = await customerAPI.getCustomers();
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

  const loadQuotations = async () => {
    try {
      const response = await quotationAPI.getQuotations();
      setQuotations(response.data || []);
    } catch (error) {
      console.error('Error loading quotations:', error);
    }
  };

  const loadOrderDetails = async () => {
    if (!order?.orderId) {
      console.warn('Cannot load order details: orderId is missing', order);
      return;
    }
    
    try {
      setLoading(true);
      console.log('Loading order details for orderId:', order.orderId);
      const response = await orderAPI.getOrder(order.orderId);
      const orderData = response.data;
      
      // Log để debug
      console.log('Order details loaded:', orderData);
      console.log('Order customer:', orderData.customer);
      console.log('Order inventory:', orderData.inventory);
      console.log('Order totalAmount:', orderData.totalAmount, 'orderAmount:', orderData.orderAmount, 'amount:', orderData.amount);
      
      // Nếu backend không trả về relationships, fetch riêng
      let customerData = orderData.customer;
      let inventoryData = orderData.inventory;
      let quotationData = orderData.quotation;
      
      if (!customerData && orderData.customerId) {
        try {
          console.log('Fetching customer details for customerId:', orderData.customerId);
          const customerResponse = await customerAPI.getCustomer(orderData.customerId);
          customerData = customerResponse.data;
          console.log('Customer details loaded:', customerData);
        } catch (error) {
          console.warn('Could not load customer details:', error);
        }
      }
      
      if (!inventoryData && orderData.inventoryId) {
        try {
          console.log('Fetching inventory details for inventoryId:', orderData.inventoryId);
          const inventoryResponse = await inventoryAPI.getInventoryById(orderData.inventoryId);
          inventoryData = inventoryResponse.data;
          console.log('Inventory details loaded:', inventoryData);
        } catch (error) {
          console.warn('Could not load inventory details:', error);
        }
      }
      
      if (!quotationData && orderData.quotationId) {
        try {
          console.log('Fetching quotation details for quotationId:', orderData.quotationId);
          const quotationResponse = await quotationAPI.getQuotation(orderData.quotationId);
          quotationData = quotationResponse.data;
          console.log('Quotation details loaded:', quotationData);
        } catch (error) {
          console.warn('Could not load quotation details:', error);
        }
      }
      
      // Fetch payments để tính toán totalAmount và depositAmount
      let payments = [];
      let calculatedTotalAmount = 0;
      let calculatedDepositAmount = 0;
      
      try {
        console.log('Fetching payments for orderId:', order.orderId);
        const paymentsResponse = await paymentAPI.getPaymentsByOrder(order.orderId);
        payments = paymentsResponse.data || [];
        console.log('Payments loaded:', payments);
        
        // Tính tổng từ payments (nếu có)
        if (payments.length > 0) {
          calculatedTotalAmount = payments
            .filter(p => p.status === 'completed' || p.status === 'pending')
            .reduce((sum, p) => sum + (parseFloat(p.amount || p.paymentAmount || 0)), 0);
          
          calculatedDepositAmount = payments
            .filter(p => (p.status === 'completed' || p.status === 'pending') && p.paymentType === 'deposit')
            .reduce((sum, p) => sum + (parseFloat(p.amount || p.paymentAmount || 0)), 0);
        }
      } catch (error) {
        console.warn('Could not load payments:', error);
      }
      
      // Tính toán totalAmount từ nhiều nguồn (ưu tiên orderData, sau đó quotation, inventory, payments)
      let totalAmount = orderData.totalAmount || orderData.orderAmount || orderData.amount || 0;
      
      // Nếu không có trong orderData, thử lấy từ quotation
      if (!totalAmount && quotationData) {
        totalAmount = quotationData.finalPrice || quotationData.totalPrice || 0;
        console.log('Using totalAmount from quotation:', totalAmount);
      }
      
      // Nếu vẫn không có, thử lấy từ inventory (ưu tiên variant.priceBase vì sellingPrice thường null)
      if (!totalAmount && inventoryData) {
        totalAmount = inventoryData.variant?.priceBase || inventoryData.sellingPrice || 0;
        if (totalAmount) {
          console.log('Using totalAmount from inventory variant:', totalAmount);
        }
      }
      
      // Nếu vẫn không có và có variantId, thử fetch variant trực tiếp
      if (!totalAmount && (orderData.variantId || inventoryData?.variantId)) {
        try {
          const variantId = orderData.variantId || inventoryData?.variantId;
          console.log('Fetching variant details for variantId:', variantId);
          const variantResponse = await vehicleAPI.getVariant(variantId);
          const variant = variantResponse.data;
          totalAmount = variant?.priceBase || 0;
          if (totalAmount) {
            console.log('Using totalAmount from fetched variant:', totalAmount);
            // Cập nhật inventoryData để có variant
            if (inventoryData) {
              inventoryData.variant = variant;
            }
          }
        } catch (error) {
          console.warn('Could not fetch variant:', error);
        }
      }
      
      // Nếu vẫn không có, dùng tổng từ payments
      if (!totalAmount && calculatedTotalAmount > 0) {
        totalAmount = calculatedTotalAmount;
        console.log('Using totalAmount from payments:', totalAmount);
      }
      
      // Tính depositAmount (ưu tiên orderData, sau đó từ payments, cuối cùng tính 10% của totalAmount)
      let depositAmount = orderData.depositAmount || 0;
      if (!depositAmount && calculatedDepositAmount > 0) {
        depositAmount = calculatedDepositAmount;
        console.log('Using depositAmount from payments:', depositAmount);
      }
      
      // Nếu vẫn không có và có totalAmount, tính 10% mặc định
      if (!depositAmount && totalAmount > 0) {
        depositAmount = Math.round(totalAmount * 0.1);
        console.log('Calculated depositAmount as 10% of totalAmount:', depositAmount);
      }
      
      // Tính balanceAmount
      const balanceAmount = orderData.balanceAmount || orderData.remainingAmount || (totalAmount - depositAmount);
      
      console.log('Final calculated values:', {
        totalAmount,
        depositAmount,
        balanceAmount,
        fromOrder: orderData.totalAmount || orderData.orderAmount || orderData.amount,
        fromQuotation: quotationData?.finalPrice || quotationData?.totalPrice,
        fromInventory: inventoryData?.variant?.priceBase || inventoryData?.sellingPrice,
        fromPayments: calculatedTotalAmount
      });
      
      setFormData({
        customerId: customerData?.customerId || orderData.customerId ? String(customerData?.customerId || orderData.customerId) : '',
        quotationId: quotationData?.quotationId || orderData.quotation?.quotationId || orderData.quotationId ? String(quotationData?.quotationId || orderData.quotation?.quotationId || orderData.quotationId) : '',
        inventoryId: inventoryData?.inventoryId || orderData.inventoryId ? String(inventoryData?.inventoryId || orderData.inventoryId) : '',
        orderDate: formatDateForInput(orderData.orderDate) || '',
        deliveryDate: formatDateForInput(orderData.deliveryDate || orderData.expectedDeliveryDate) || '',
        totalAmount: totalAmount > 0 ? String(totalAmount) : '',
        depositAmount: depositAmount > 0 ? String(depositAmount) : '',
        balanceAmount: balanceAmount > 0 ? String(balanceAmount) : '',
        status: orderData.status || 'pending',
        notes: orderData.notes || ''
      });
      
      // Cập nhật order object để có dữ liệu mới nhất cho phần hiển thị info
      if (order) {
        Object.assign(order, {
          ...orderData,
          customer: customerData || orderData.customer,
          inventory: inventoryData || orderData.inventory,
          quotation: quotationData || orderData.quotation,
          totalAmount: totalAmount || orderData.totalAmount,
          orderAmount: orderData.orderAmount,
          amount: orderData.amount,
          depositAmount: depositAmount || orderData.depositAmount,
          balanceAmount: balanceAmount || orderData.balanceAmount
        });
      }
    } catch (error) {
      console.error('Error loading order details:', error);
      console.error('Error response:', error.response);
      toast.error('Không thể tải thông tin đơn hàng');
      
      // Fallback: dùng dữ liệu từ order object nếu có
      if (order) {
        const totalAmount = order.totalAmount || order.orderAmount || order.amount || 0;
        let depositAmount = order.depositAmount || 0;
        // Nếu không có depositAmount và có totalAmount, tính 10% mặc định
        if (!depositAmount && totalAmount > 0) {
          depositAmount = Math.round(totalAmount * 0.1);
        }
        const balanceAmount = order.balanceAmount || order.remainingAmount || (totalAmount - depositAmount);
        
        setFormData({
          customerId: order.customer?.customerId || order.customerId ? String(order.customer?.customerId || order.customerId) : '',
          quotationId: order.quotation?.quotationId || order.quotationId ? String(order.quotation?.quotationId || order.quotationId) : '',
          inventoryId: order.inventory?.inventoryId || order.inventoryId ? String(order.inventory?.inventoryId || order.inventoryId) : '',
          orderDate: formatDateForInput(order.orderDate) || '',
          deliveryDate: formatDateForInput(order.deliveryDate || order.expectedDeliveryDate) || '',
          totalAmount: totalAmount ? String(totalAmount) : '',
          depositAmount: depositAmount ? String(depositAmount) : '',
          balanceAmount: balanceAmount ? String(balanceAmount) : '',
          status: order.status || 'pending',
          notes: order.notes || ''
        });
      }
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
    if (name === 'totalAmount' || name === 'depositAmount') {
      const totalAmount = parseFloat(name === 'totalAmount' ? value : formData.totalAmount) || 0;
      const depositAmount = parseFloat(name === 'depositAmount' ? value : formData.depositAmount) || 0;
      const balanceAmount = totalAmount - depositAmount;
      setFormData(prev => ({
        ...prev,
        balanceAmount: balanceAmount.toString()
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      
      // Required fields (theo FIELD_REFERENCE_GUIDE.md line 192-213)
      // Tất cả fields trong OrderRequest đều optional, nhưng một số có default values
      const submitData = {};
      
      // Chỉ thêm fields nếu có giá trị (không gửi null/undefined/empty)
      if (formData.quotationId) {
        submitData.quotationId = typeof formData.quotationId === 'string' ? parseInt(formData.quotationId, 10) : formData.quotationId;
      }
      if (formData.customerId) {
        submitData.customerId = typeof formData.customerId === 'string' ? parseInt(formData.customerId, 10) : formData.customerId;
      }
      if (formData.inventoryId) {
        submitData.inventoryId = typeof formData.inventoryId === 'string' ? parseInt(formData.inventoryId, 10) : formData.inventoryId;
      }
      if (formData.orderDate?.trim()) {
        submitData.orderDate = formData.orderDate.trim(); // Format: YYYY-MM-DD
      }
      if (formData.deliveryDate?.trim()) {
        submitData.deliveryDate = formData.deliveryDate.trim(); // Format: YYYY-MM-DD
      }
      if (formData.totalAmount) {
        submitData.totalAmount = parseFloat(formData.totalAmount);
      }
      if (formData.depositAmount) {
        submitData.depositAmount = parseFloat(formData.depositAmount);
      }
      if (formData.balanceAmount) {
        submitData.balanceAmount = parseFloat(formData.balanceAmount);
      }
      if (formData.status?.trim()) {
        submitData.status = formData.status.trim(); // lowercase
      }
      if (formData.notes?.trim()) {
        submitData.notes = formData.notes.trim();
      }
      if (formData.specialRequests?.trim()) {
        submitData.specialRequests = formData.specialRequests.trim();
      }
      
      await onSave(order.orderId, submitData);
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
                value={formData.customerId ? String(formData.customerId) : ''}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn khách hàng</option>
                {customers.map(customer => (
                  <option key={customer.customerId} value={String(customer.customerId)}>
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
                value={formData.quotationId ? String(formData.quotationId) : ''}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
              >
                <option value="">Chọn báo giá (tùy chọn)</option>
                {quotations.map(quotation => (
                  <option key={quotation.quotationId} value={String(quotation.quotationId)}>
                    #{quotation.quotationId} - {quotation.customer?.firstName} {quotation.customer?.lastName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="inventoryId">Xe trong kho</label>
              <select
                id="inventoryId"
                name="inventoryId"
                value={formData.inventoryId ? String(formData.inventoryId) : ''}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-select"
                required
              >
                <option value="">Chọn xe trong kho</option>
                {vehicles.map(vehicle => (
                  <option key={vehicle.inventoryId} value={String(vehicle.inventoryId)}>
                    {vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName} {vehicle.variant?.variantName} - VIN: {vehicle.vin || 'N/A'}
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
              <label htmlFor="totalAmount">Tổng giá trị đơn hàng (VNĐ)</label>
              <input
                type="number"
                id="totalAmount"
                name="totalAmount"
                value={formData.totalAmount}
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
              <label htmlFor="balanceAmount">Số tiền còn lại (VNĐ)</label>
              <input
                type="number"
                id="balanceAmount"
                name="balanceAmount"
                value={formData.balanceAmount}
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
                <option value="pending">Chờ xử lý</option>
                <option value="quoted">Đã có báo giá</option>
                <option value="confirmed">Đã xác nhận</option>
                <option value="paid">Đã thanh toán</option>
                <option value="delivered">Đã giao hàng</option>
                <option value="completed">Hoàn tất</option>
                <option value="rejected">Đã từ chối</option>
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
                placeholder="Ghi chú về đơn hàng..."
              />
            </div>
          </div>

          {mode === 'view' && order && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {order.createdAt ? new Date(order.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
              <div className="info-item">
                <DollarSign size={16} />
                <span>Tổng giá trị: {
                  formData.totalAmount && Number(formData.totalAmount) > 0
                    ? new Intl.NumberFormat('vi-VN').format(Number(formData.totalAmount)) + ' VNĐ'
                    : (order.totalAmount || order.orderAmount || order.amount)
                      ? new Intl.NumberFormat('vi-VN').format(Number(order.totalAmount || order.orderAmount || order.amount)) + ' VNĐ'
                      : 'N/A'
                }</span>
              </div>
              <div className="info-item">
                <FileText size={16} />
                <span>Mã đơn hàng: #{order.orderId || order.orderNumber || 'N/A'}</span>
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
