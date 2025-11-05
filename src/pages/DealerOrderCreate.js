import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  ShoppingCart, 
  User, 
  FileText, 
  CheckCircle,
  ArrowRight,
  ArrowLeft,
  Plus,
  X,
  Search,
  Car
} from 'lucide-react';
import { dealerOrderAPI, vehicleAPI, dealerAPI, customerAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerOrderCreate.css';

const DealerOrderCreate = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  
  // Data states
  const [variants, setVariants] = useState([]);
  const [colors, setColors] = useState([]);
  const [dealers, setDealers] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [filteredVariants, setFilteredVariants] = useState([]);
  const [variantSearch, setVariantSearch] = useState('');
  
  // Form states - theo guide
  const [formData, setFormData] = useState({
    // Step 1: Vehicle Selection
    items: [], // Array of { variantId, colorId, quantity, unitPrice?, discountPercentage?, notes? }
    
    // Step 2: Customer Selection (optional)
    customerId: null,
    
    // Step 3: Order Information
    dealerId: '', // Auto từ current user nếu là dealer
    evmStaffId: '', // Optional, cho EVM_STAFF/ADMIN
    orderDate: new Date().toISOString().split('T')[0],
    expectedDeliveryDate: '',
    orderType: 'PURCHASE', // PURCHASE, RESERVE, SAMPLE
    priority: 'NORMAL', // LOW, NORMAL, HIGH, URGENT
    paymentTerms: '', // NET_15, NET_30, NET_45, NET_60, CASH_ON_DELIVERY, ADVANCE_PAYMENT
    deliveryTerms: '', // FOB_FACTORY, FOB_DESTINATION, EX_WORKS, CIF, DDP
    notes: ''
  });
  
  // Current item being added
  const [currentItem, setCurrentItem] = useState({
    variantId: '',
    colorId: '',
    quantity: 1,
    unitPrice: '',
    discountPercentage: 0,
    notes: ''
  });

  const steps = [
    { id: 1, title: 'Chọn xe', description: 'Chọn variant và màu sắc' },
    { id: 2, title: 'Chọn khách hàng', description: 'Chọn hoặc tạo khách hàng (tùy chọn)' },
    { id: 3, title: 'Thông tin đơn hàng', description: 'Nhập thông tin chi tiết đơn hàng' },
    { id: 4, title: 'Xác nhận', description: 'Xem lại và tạo đơn hàng' }
  ];

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    // Filter variants based on search
    if (variantSearch.trim() === '') {
      setFilteredVariants(variants);
    } else {
      const searchLower = variantSearch.toLowerCase();
      setFilteredVariants(variants.filter(v => 
        v.variantName?.toLowerCase().includes(searchLower) ||
        v.model?.modelName?.toLowerCase().includes(searchLower) ||
        v.model?.brand?.brandName?.toLowerCase().includes(searchLower)
      ));
    }
  }, [variantSearch, variants]);

  useEffect(() => {
    // Auto-set dealerId if user is dealer
    if (user && (user.role === 'dealer_manager' || user.role === 'dealer_staff')) {
      if (user.dealerId) {
        setFormData(prev => ({ ...prev, dealerId: user.dealerId }));
      }
    }
  }, [user]);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [variantsRes, colorsRes, dealersRes, customersRes] = await Promise.all([
        vehicleAPI.getActiveVariants().catch(() => ({ data: [] })),
        vehicleAPI.getActiveColors().catch(() => ({ data: [] })),
        dealerAPI.getDealers().catch(() => ({ data: [] })),
        customerAPI.getCustomers().catch(() => ({ data: [] }))
      ]);
      
      setVariants(variantsRes.data || []);
      setColors(colorsRes.data || []);
      setDealers(dealersRes.data || []);
      setCustomers(customersRes.data || []);
      setFilteredVariants(variantsRes.data || []);
    } catch (error) {
      console.error('Error loading initial data:', error);
      toast.error('Không thể tải dữ liệu ban đầu');
    } finally {
      setLoading(false);
    }
  };

  // Step 1: Vehicle Selection
  const handleAddItem = () => {
    if (!currentItem.variantId || !currentItem.colorId || !currentItem.quantity || currentItem.quantity < 1) {
      toast.error('Vui lòng chọn variant, màu sắc và số lượng (tối thiểu 1)');
      return;
    }

    const variant = variants.find(v => v.variantId === parseInt(currentItem.variantId));
    const unitPrice = currentItem.unitPrice ? parseFloat(currentItem.unitPrice) : (variant?.priceBase || 0);

    setFormData(prev => ({
      ...prev,
      items: [...prev.items, {
        variantId: parseInt(currentItem.variantId),
        colorId: parseInt(currentItem.colorId),
        quantity: parseInt(currentItem.quantity),
        unitPrice: unitPrice,
        discountPercentage: currentItem.discountPercentage ? parseFloat(currentItem.discountPercentage) : undefined,
        notes: currentItem.notes || undefined
      }]
    }));

    setCurrentItem({
      variantId: '',
      colorId: '',
      quantity: 1,
      unitPrice: '',
      discountPercentage: 0,
      notes: ''
    });
    
    toast.success('Đã thêm xe vào đơn hàng');
  };

  const handleRemoveItem = (index) => {
    setFormData(prev => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index)
    }));
  };

  const handleItemChange = (e) => {
    const { name, value } = e.target;
    setCurrentItem(prev => ({ ...prev, [name]: value }));
    
    // Auto-set unitPrice from variant priceBase
    if (name === 'variantId' && value) {
      const variant = variants.find(v => v.variantId === parseInt(value));
      if (variant && variant.priceBase) {
        setCurrentItem(prev => ({ ...prev, unitPrice: variant.priceBase }));
      }
    }
  };

  // Step Navigation
  const handleNext = () => {
    if (currentStep === 1) {
      // Validate: at least 1 item
      if (formData.items.length === 0) {
        toast.error('Vui lòng thêm ít nhất một xe vào đơn hàng');
        return;
      }
      setCurrentStep(2);
    } else if (currentStep === 2) {
      // Step 2 is optional, can skip
      setCurrentStep(3);
    } else if (currentStep === 3) {
      // Validate order info
      if (!formData.dealerId) {
        toast.error('Vui lòng chọn đại lý');
        return;
      }
      if (!formData.orderDate) {
        toast.error('Vui lòng chọn ngày đặt hàng');
        return;
      }
      if (formData.expectedDeliveryDate && formData.expectedDeliveryDate < formData.orderDate) {
        toast.error('Ngày giao hàng dự kiến không được nhỏ hơn ngày đặt hàng');
        return;
      }
      setCurrentStep(4);
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // Step 4: Submit Order
  const handleSubmit = async () => {
    try {
      setSubmitting(true);
      
      // Build request according to guide
      const request = {
        dealerId: formData.dealerId,
        evmStaffId: formData.evmStaffId || undefined,
        orderDate: formData.orderDate,
        expectedDeliveryDate: formData.expectedDeliveryDate || undefined,
        orderType: formData.orderType,
        priority: formData.priority,
        paymentTerms: formData.paymentTerms || undefined,
        deliveryTerms: formData.deliveryTerms || undefined,
        notes: formData.notes || undefined,
        items: formData.items.map(item => ({
          variantId: item.variantId,
          colorId: item.colorId,
          quantity: item.quantity,
          unitPrice: item.unitPrice || undefined,
          discountPercentage: item.discountPercentage || undefined,
          notes: item.notes || undefined
        }))
      };

      console.log('Submitting dealer order:', request);
      
      const response = await dealerOrderAPI.createDetailedOrder(request);
      
      toast.success(response.data?.message || 'Tạo đơn hàng thành công!');
      
      // Redirect to order detail page
      if (response.data?.dealerOrder?.dealerOrderId) {
        navigate(`/admin/dealer-orders/${response.data.dealerOrder.dealerOrderId}`);
      } else {
        navigate('/admin/dealer-orders');
      }
    } catch (error) {
      console.error('Error creating dealer order:', error);
      const errorMessage = error.response?.data?.error || error.message || 'Không thể tạo đơn hàng';
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  // Render Step 1: Vehicle Selection
  const renderStep1 = () => {
    const getVariantDisplayName = (variant) => {
      if (!variant) return '';
      const brand = variant.model?.brand?.brandName || '';
      const model = variant.model?.modelName || '';
      const variantName = variant.variantName || '';
      return `${brand} ${model} ${variantName}`.trim();
    };

    return (
      <div className="order-wizard-step">
        <h2 className="step-title">Chọn xe</h2>
        <p className="step-description">Chọn variant, màu sắc và số lượng cho từng dòng sản phẩm</p>
        
        {/* Add Vehicle Form */}
        <div className="add-vehicle-form">
          <div className="form-row">
            <div className="form-group">
              <label>Tìm kiếm variant</label>
              <div className="search-input-wrapper">
                <Search size={18} className="search-icon" />
                <input
                  type="text"
                  placeholder="Tìm theo tên, model, brand..."
                  value={variantSearch}
                  onChange={(e) => setVariantSearch(e.target.value)}
                  className="form-input"
                />
              </div>
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Phiên bản xe *</label>
              <select
                name="variantId"
                value={currentItem.variantId}
                onChange={handleItemChange}
                className="form-select"
                required
              >
                <option value="">Chọn phiên bản xe</option>
                {filteredVariants.map(variant => (
                  <option key={variant.variantId} value={variant.variantId}>
                    {getVariantDisplayName(variant)} - {variant.priceBase?.toLocaleString('vi-VN')} VNĐ
                  </option>
                ))}
              </select>
            </div>
            
            <div className="form-group">
              <label>Màu sắc *</label>
              <select
                name="colorId"
                value={currentItem.colorId}
                onChange={handleItemChange}
                className="form-select"
                required
              >
                <option value="">Chọn màu</option>
                {colors.map(color => (
                  <option key={color.colorId} value={color.colorId}>
                    {color.colorName} {color.colorCode ? `(${color.colorCode})` : ''}
                  </option>
                ))}
              </select>
            </div>
            
            <div className="form-group">
              <label>Số lượng *</label>
              <input
                type="number"
                name="quantity"
                value={currentItem.quantity}
                onChange={handleItemChange}
                min="1"
                max="100"
                className="form-input"
                required
              />
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Đơn giá (VNĐ)</label>
              <input
                type="number"
                name="unitPrice"
                value={currentItem.unitPrice}
                onChange={handleItemChange}
                className="form-input"
                placeholder="Tự động từ giá gốc"
              />
            </div>
            
            <div className="form-group">
              <label>Giảm giá (%)</label>
              <input
                type="number"
                name="discountPercentage"
                value={currentItem.discountPercentage}
                onChange={handleItemChange}
                min="0"
                max="100"
                className="form-input"
              />
            </div>
            
            <div className="form-group">
              <label>Ghi chú</label>
              <input
                type="text"
                name="notes"
                value={currentItem.notes}
                onChange={handleItemChange}
                className="form-input"
              />
            </div>
          </div>
          
          <button
            type="button"
            onClick={handleAddItem}
            className="btn btn-primary"
            style={{ marginTop: '16px' }}
          >
            <Plus size={18} /> Thêm vào đơn hàng
          </button>
        </div>
        
        {/* Selected Items List */}
        <div className="selected-items">
          <h3>Danh sách xe đã chọn ({formData.items.length})</h3>
          {formData.items.length === 0 ? (
            <p className="empty-message">Chưa có xe nào được thêm vào đơn hàng</p>
          ) : (
            <div className="items-list">
              {formData.items.map((item, index) => {
                const variant = variants.find(v => v.variantId === item.variantId);
                const color = colors.find(c => c.colorId === item.colorId);
                const subtotal = (item.unitPrice || 0) * item.quantity;
                const discount = item.discountPercentage ? (subtotal * item.discountPercentage / 100) : 0;
                const finalPrice = subtotal - discount;
                
                return (
                  <div key={index} className="item-card">
                    <div className="item-info">
                      <div className="item-header">
                        <h4>{getVariantDisplayName(variant)}</h4>
                        <button
                          type="button"
                          onClick={() => handleRemoveItem(index)}
                          className="btn-remove"
                        >
                          <X size={18} />
                        </button>
                      </div>
                      <div className="item-details">
                        <span>Màu: {color?.colorName || 'N/A'}</span>
                        <span>Số lượng: {item.quantity}</span>
                        <span>Đơn giá: {item.unitPrice?.toLocaleString('vi-VN')} VNĐ</span>
                        {item.discountPercentage && (
                          <span>Giảm giá: {item.discountPercentage}%</span>
                        )}
                        <span className="item-total">Tổng: {finalPrice.toLocaleString('vi-VN')} VNĐ</span>
                      </div>
                      {item.notes && (
                        <div className="item-notes">Ghi chú: {item.notes}</div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render Step 2: Customer Selection (Optional)
  const renderStep2 = () => {
    return (
      <div className="order-wizard-step">
        <h2 className="step-title">Chọn khách hàng (Tùy chọn)</h2>
        <p className="step-description">Bước này có thể bỏ qua nếu không cần thiết</p>
        
        <div className="form-group">
          <label>Khách hàng</label>
          <select
            name="customerId"
            value={formData.customerId || ''}
            onChange={(e) => setFormData(prev => ({ ...prev, customerId: e.target.value || null }))}
            className="form-select"
          >
            <option value="">Không chọn khách hàng</option>
            {customers.map(customer => (
              <option key={customer.customerId} value={customer.customerId}>
                {customer.firstName} {customer.lastName} - {customer.phone || customer.email || 'N/A'}
              </option>
            ))}
          </select>
        </div>
        
        <div className="step-note">
          <p>💡 Bạn có thể bỏ qua bước này và tiếp tục với bước tiếp theo</p>
        </div>
      </div>
    );
  };

  // Render Step 3: Order Information
  const renderStep3 = () => {
    const isDealer = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
    const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
    
    return (
      <div className="order-wizard-step">
        <h2 className="step-title">Thông tin đơn hàng</h2>
        <p className="step-description">Nhập thông tin chi tiết đơn hàng đại lý</p>
        
        <div className="form-grid">
          {isEVMStaff && (
            <div className="form-group">
              <label>Đại lý *</label>
              <select
                name="dealerId"
                value={formData.dealerId}
                onChange={handleFormChange}
                className="form-select"
                required
              >
                <option value="">Chọn đại lý</option>
                {dealers.map(dealer => (
                  <option key={dealer.dealerId} value={dealer.dealerId}>
                    {dealer.dealerName} ({dealer.dealerCode})
                  </option>
                ))}
              </select>
            </div>
          )}
          
          {isEVMStaff && (
            <div className="form-group">
              <label>Nhân viên EVM</label>
              <select
                name="evmStaffId"
                value={formData.evmStaffId}
                onChange={handleFormChange}
                className="form-select"
              >
                <option value="">Không chọn</option>
                {/* TODO: Load EVM staff list if needed */}
              </select>
            </div>
          )}
          
          <div className="form-group">
            <label>Ngày đặt hàng *</label>
            <input
              type="date"
              name="orderDate"
              value={formData.orderDate}
              onChange={handleFormChange}
              className="form-input"
              required
            />
          </div>
          
          <div className="form-group">
            <label>Ngày giao hàng dự kiến</label>
            <input
              type="date"
              name="expectedDeliveryDate"
              value={formData.expectedDeliveryDate}
              onChange={handleFormChange}
              className="form-input"
              min={formData.orderDate}
            />
          </div>
          
          <div className="form-group">
            <label>Loại đơn hàng</label>
            <select
              name="orderType"
              value={formData.orderType}
              onChange={handleFormChange}
              className="form-select"
            >
              <option value="PURCHASE">Mua hàng</option>
              <option value="RESERVE">Đặt trước</option>
              <option value="SAMPLE">Mẫu</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Độ ưu tiên</label>
            <select
              name="priority"
              value={formData.priority}
              onChange={handleFormChange}
              className="form-select"
            >
              <option value="LOW">Thấp</option>
              <option value="NORMAL">Bình thường</option>
              <option value="HIGH">Cao</option>
              <option value="URGENT">Khẩn cấp</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Điều khoản thanh toán</label>
            <select
              name="paymentTerms"
              value={formData.paymentTerms}
              onChange={handleFormChange}
              className="form-select"
            >
              <option value="">Không chọn</option>
              <option value="NET_15">NET 15</option>
              <option value="NET_30">NET 30</option>
              <option value="NET_45">NET 45</option>
              <option value="NET_60">NET 60</option>
              <option value="CASH_ON_DELIVERY">Thanh toán khi nhận hàng</option>
              <option value="ADVANCE_PAYMENT">Thanh toán trước</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Điều khoản giao hàng</label>
            <select
              name="deliveryTerms"
              value={formData.deliveryTerms}
              onChange={handleFormChange}
              className="form-select"
            >
              <option value="">Không chọn</option>
              <option value="FOB_FACTORY">FOB Factory</option>
              <option value="FOB_DESTINATION">FOB Destination</option>
              <option value="EX_WORKS">EX Works</option>
              <option value="CIF">CIF</option>
              <option value="DDP">DDP</option>
            </select>
          </div>
          
          <div className="form-group full-width">
            <label>Ghi chú</label>
            <textarea
              name="notes"
              value={formData.notes}
              onChange={handleFormChange}
              className="form-textarea"
              rows="4"
              placeholder="Nhập ghi chú cho đơn hàng..."
            />
          </div>
        </div>
      </div>
    );
  };

  // Render Step 4: Confirmation
  const renderStep4 = () => {
    const totalQuantity = formData.items.reduce((sum, item) => sum + item.quantity, 0);
    const totalAmount = formData.items.reduce((sum, item) => {
      const subtotal = (item.unitPrice || 0) * item.quantity;
      const discount = item.discountPercentage ? (subtotal * item.discountPercentage / 100) : 0;
      return sum + (subtotal - discount);
    }, 0);
    
    const dealer = dealers.find(d => d.dealerId === formData.dealerId);
    const customer = customers.find(c => c.customerId === formData.customerId);
    
    return (
      <div className="order-wizard-step">
        <h2 className="step-title">Xác nhận đơn hàng</h2>
        <p className="step-description">Xem lại thông tin trước khi tạo đơn hàng</p>
        
        <div className="confirmation-summary">
          <div className="summary-section">
            <h3>Thông tin đơn hàng</h3>
            <div className="summary-item">
              <span className="label">Đại lý:</span>
              <span className="value">{dealer?.dealerName || 'N/A'} ({dealer?.dealerCode || 'N/A'})</span>
            </div>
            <div className="summary-item">
              <span className="label">Ngày đặt hàng:</span>
              <span className="value">{formData.orderDate}</span>
            </div>
            {formData.expectedDeliveryDate && (
              <div className="summary-item">
                <span className="label">Ngày giao dự kiến:</span>
                <span className="value">{formData.expectedDeliveryDate}</span>
              </div>
            )}
            <div className="summary-item">
              <span className="label">Loại đơn hàng:</span>
              <span className="value">
                {formData.orderType === 'PURCHASE' ? 'Mua hàng' : 
                 formData.orderType === 'RESERVE' ? 'Đặt trước' : 'Mẫu'}
              </span>
            </div>
            <div className="summary-item">
              <span className="label">Độ ưu tiên:</span>
              <span className="value">
                {formData.priority === 'LOW' ? 'Thấp' :
                 formData.priority === 'NORMAL' ? 'Bình thường' :
                 formData.priority === 'HIGH' ? 'Cao' : 'Khẩn cấp'}
              </span>
            </div>
            {formData.paymentTerms && (
              <div className="summary-item">
                <span className="label">Điều khoản thanh toán:</span>
                <span className="value">{formData.paymentTerms}</span>
              </div>
            )}
            {formData.deliveryTerms && (
              <div className="summary-item">
                <span className="label">Điều khoản giao hàng:</span>
                <span className="value">{formData.deliveryTerms}</span>
              </div>
            )}
            {formData.notes && (
              <div className="summary-item">
                <span className="label">Ghi chú:</span>
                <span className="value">{formData.notes}</span>
              </div>
            )}
          </div>
          
          {customer && (
            <div className="summary-section">
              <h3>Khách hàng</h3>
              <div className="summary-item">
                <span className="label">Tên:</span>
                <span className="value">{customer.firstName} {customer.lastName}</span>
              </div>
              {customer.phone && (
                <div className="summary-item">
                  <span className="label">Điện thoại:</span>
                  <span className="value">{customer.phone}</span>
                </div>
              )}
              {customer.email && (
                <div className="summary-item">
                  <span className="label">Email:</span>
                  <span className="value">{customer.email}</span>
                </div>
              )}
            </div>
          )}
          
          <div className="summary-section">
            <h3>Danh sách xe ({formData.items.length} loại)</h3>
            <div className="items-summary">
              {formData.items.map((item, index) => {
                const variant = variants.find(v => v.variantId === item.variantId);
                const color = colors.find(c => c.colorId === item.colorId);
                const subtotal = (item.unitPrice || 0) * item.quantity;
                const discount = item.discountPercentage ? (subtotal * item.discountPercentage / 100) : 0;
                const finalPrice = subtotal - discount;
                
                return (
                  <div key={index} className="item-summary-card">
                    <div className="item-summary-header">
                      <span className="item-number">{index + 1}</span>
                      <h4>{variant?.model?.brand?.brandName} {variant?.model?.modelName} {variant?.variantName}</h4>
                    </div>
                    <div className="item-summary-details">
                      <span>Màu: {color?.colorName}</span>
                      <span>Số lượng: {item.quantity}</span>
                      <span>Đơn giá: {item.unitPrice?.toLocaleString('vi-VN')} VNĐ</span>
                      {item.discountPercentage && (
                        <span>Giảm giá: {item.discountPercentage}% (-{discount.toLocaleString('vi-VN')} VNĐ)</span>
                      )}
                      <span className="item-final-price">Thành tiền: {finalPrice.toLocaleString('vi-VN')} VNĐ</span>
                    </div>
                    {item.notes && (
                      <div className="item-summary-notes">Ghi chú: {item.notes}</div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
          
          <div className="summary-section total-section">
            <div className="total-item">
              <span className="label">Tổng số lượng:</span>
              <span className="value">{totalQuantity} xe</span>
            </div>
            <div className="total-item">
              <span className="label">Tổng thành tiền:</span>
              <span className="value total-amount">{totalAmount.toLocaleString('vi-VN')} VNĐ</span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-order-management">
      <div className="page-header">
        <div className="page-title">
          <ShoppingCart className="title-icon" />
          <h1>Tạo đơn hàng đại lý</h1>
        </div>
        <p>Tạo đơn hàng đại lý mới với 4 bước đơn giản</p>
      </div>

      <div className="order-wizard">
        {/* Progress Indicator */}
        <div className="wizard-progress">
          {steps.map((step, index) => (
            <div key={step.id} className={`progress-step ${currentStep === step.id ? 'active' : ''} ${currentStep > step.id ? 'completed' : ''}`}>
              <div className="step-number">
                {currentStep > step.id ? <CheckCircle size={20} /> : step.id}
              </div>
              <div className="step-info">
                <div className="step-title-small">{step.title}</div>
                <div className="step-description-small">{step.description}</div>
              </div>
              {index < steps.length - 1 && <div className="step-connector" />}
            </div>
          ))}
        </div>

        {/* Step Content */}
        <div className="wizard-content">
          {currentStep === 1 && renderStep1()}
          {currentStep === 2 && renderStep2()}
          {currentStep === 3 && renderStep3()}
          {currentStep === 4 && renderStep4()}
        </div>

        {/* Navigation */}
        <div className="wizard-navigation">
          <button
            type="button"
            onClick={handleBack}
            disabled={currentStep === 1}
            className="btn btn-secondary"
          >
            <ArrowLeft size={18} /> Quay lại
          </button>
          
          {currentStep < 4 ? (
            <button
              type="button"
              onClick={handleNext}
              className="btn btn-primary"
            >
              Tiếp theo <ArrowRight size={18} />
            </button>
          ) : (
            <button
              type="button"
              onClick={handleSubmit}
              disabled={submitting}
              className="btn btn-success"
            >
              {submitting ? 'Đang tạo...' : 'Tạo đơn hàng'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default DealerOrderCreate;
