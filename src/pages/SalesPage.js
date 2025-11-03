import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { 
  Car, 
  User, 
  FileText, 
  CheckCircle,
  ArrowRight,
  ArrowLeft,
  Search,
  Plus
} from 'lucide-react';
import { 
  customerAPI, 
  orderAPI, 
  inventoryAPI,
  quotationAPI 
} from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { useAuth } from '../contexts/AuthContext';
import '../styles/common.css';
import './SalesPage.css';

const SalesPage = () => {
  const { user, hasAnyRole } = useAuth();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  
  // Data states
  const [vehicles, setVehicles] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [inventory, setInventory] = useState([]);
  
  // Form states
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [selectedInventory, setSelectedInventory] = useState(null);
  const [orderData, setOrderData] = useState({
    paymentMethod: 'cash',
    depositAmount: 0,
    notes: '',
    deliveryDate: '',
    specialRequests: ''
  });
  
  // Search states
  const [vehicleSearch, setVehicleSearch] = useState('');
  const [customerSearch, setCustomerSearch] = useState('');
  const [showNewCustomerForm, setShowNewCustomerForm] = useState(false);
  const [newCustomer, setNewCustomer] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    province: '',
    postalCode: ''
  });

  const steps = [
    { id: 1, title: 'Chọn xe', icon: Car, description: 'Chọn xe từ kho hàng đại lý' },
    { id: 2, title: 'Chọn khách hàng', icon: User, description: 'Chọn hoặc tạo khách hàng mới' },
    { id: 3, title: 'Thông tin đơn hàng', icon: FileText, description: 'Nhập thông tin đơn hàng đại lý' },
    { id: 4, title: 'Xác nhận', icon: CheckCircle, description: 'Xác nhận và tạo đơn hàng đại lý' }
  ];

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [customersRes, inventoryRes] = await Promise.all([
        customerAPI.getCustomers(),
        inventoryAPI.getInventoryByStatus('available')
      ]);
      
      setCustomers(customersRes.data || []);
      setInventory(inventoryRes.data || []);
      
      // Extract unique vehicles from inventory
      const uniqueVehicles = [];
      const vehicleMap = new Map();
      
      inventoryRes.data?.forEach(item => {
        if (item.variant && !vehicleMap.has(item.variant.variantId)) {
          vehicleMap.set(item.variant.variantId, item.variant);
          uniqueVehicles.push(item.variant);
        }
      });
      
      setVehicles(uniqueVehicles);
    } catch (error) {
      console.error('Error loading initial data:', error);
      toast.error('Không thể tải dữ liệu ban đầu');
    } finally {
      setLoading(false);
    }
  };

  const filteredVehicles = vehicles.filter(vehicle =>
    (vehicle.variantName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase()) ||
    (vehicle.model?.modelName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase()) ||
    (vehicle.model?.brand?.brandName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase())
  );

  const filteredCustomers = customers.filter(customer =>
    customer.firstName?.toLowerCase().includes(customerSearch.toLowerCase()) ||
    customer.lastName?.toLowerCase().includes(customerSearch.toLowerCase()) ||
    customer.email?.toLowerCase().includes(customerSearch.toLowerCase()) ||
    customer.phone?.includes(customerSearch)
  );

  const availableInventory = inventory.filter(item => 
    item.variant?.variantId === selectedVehicle?.variantId && 
    item.status === 'available'
  );

  const getVehicleInventoryCount = (variantId) => {
    return inventory.filter(item => 
      item.variant?.variantId === variantId && 
      item.status === 'available'
    ).length;
  };

  const calculateTotalPrice = () => {
    if (!selectedInventory) return 0;
    return selectedInventory.sellingPrice || 0;
  };

  const calculateDepositAmount = () => {
    const total = calculateTotalPrice();
    return Math.round(total * 0.1); // 10% deposit
  };

  const handleNextStep = () => {
    if (currentStep < steps.length) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleCreateNewCustomer = async () => {
    try {
      setLoading(true);
      const response = await customerAPI.createCustomer(newCustomer);
      const createdCustomer = response.data;
      
      setCustomers([...customers, createdCustomer]);
      setSelectedCustomer(createdCustomer);
      setShowNewCustomerForm(false);
      setNewCustomer({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        city: '',
        province: '',
        postalCode: ''
      });
      
      toast.success('Tạo khách hàng mới thành công');
    } catch (error) {
      console.error('Error creating customer:', error);
      toast.error('Không thể tạo khách hàng mới');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitOrder = async () => {
    try {
      setLoading(true);
      
      // Validate required data
      if (!selectedCustomer?.customerId) {
        toast.error('Vui lòng chọn khách hàng');
        return;
      }
      if (!selectedVehicle?.variantId) {
        toast.error('Vui lòng chọn xe');
        return;
      }
      if (!selectedInventory?.inventoryId) {
        toast.error('Vui lòng chọn xe cụ thể từ kho');
        return;
      }
      if (!user?.userId) {
        toast.error('Không tìm thấy thông tin người dùng');
        return;
      }
      
      // Create quotation first
      const quotationData = {
        customerId: selectedCustomer.customerId,
        userId: user.userId,
        variantId: selectedVehicle.variantId,
        colorId: selectedInventory.color?.colorId || null,
        quotationDate: new Date().toISOString().split('T')[0],
        totalPrice: calculateTotalPrice(),
        discountAmount: 0,
        finalPrice: calculateTotalPrice(),
        validityDays: 7,
        status: 'accepted',
        notes: orderData.notes || ''
      };

      console.log('Creating quotation with data:', quotationData);
      const quotationResponse = await quotationAPI.createQuotation(quotationData);
      const quotation = quotationResponse.data;
      console.log('Quotation created:', quotation);

      // Try to convert quotation to order first
      try {
        console.log('Converting quotation to order...');
        const orderResponse = await quotationAPI.convertToOrder(quotation.quotationId);
        console.log('Order created from quotation:', orderResponse.data);
      } catch (convertError) {
        console.log('Convert to order failed, trying direct order creation...');
        console.error('Convert error:', convertError);
        
        // Fallback: Create order directly
        const orderDataToSubmit = {
          quotationId: quotation.quotationId,
          customerId: selectedCustomer.customerId,
          userId: user.userId,
          inventoryId: selectedInventory.inventoryId,
          orderDate: new Date().toISOString().split('T')[0],
          status: 'pending',
          totalAmount: calculateTotalPrice(),
          depositAmount: orderData.depositAmount || calculateDepositAmount(),
          balanceAmount: calculateTotalPrice() - (orderData.depositAmount || calculateDepositAmount()),
          paymentMethod: orderData.paymentMethod,
          notes: orderData.notes || '',
          deliveryDate: orderData.deliveryDate || null,
          specialRequests: orderData.specialRequests || ''
        };

        console.log('Creating order with data:', orderDataToSubmit);
        const orderResponse = await orderAPI.createOrder(orderDataToSubmit);
        console.log('Order created:', orderResponse.data);
      }
      
      toast.success('Tạo đơn hàng thành công!');
      
      // Reset form
      setCurrentStep(1);
      setSelectedVehicle(null);
      setSelectedCustomer(null);
      setSelectedInventory(null);
      setOrderData({
        paymentMethod: 'cash',
        depositAmount: 0,
        notes: '',
        deliveryDate: '',
        specialRequests: ''
      });
      
    } catch (error) {
      console.error('Error creating order:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      
      if (error.response?.status === 400) {
        toast.error(`Lỗi dữ liệu: ${error.response?.data?.message || 'Dữ liệu không hợp lệ'}`);
      } else {
        toast.error('Không thể tạo đơn hàng');
      }
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="step-content">
            <h3>Chọn xe từ kho hàng đại lý</h3>
            <div className="search-box">
              <Search size={20} />
              <input
                type="text"
                placeholder="Tìm kiếm xe theo tên, model, thương hiệu..."
                value={vehicleSearch}
                onChange={(e) => setVehicleSearch(e.target.value)}
              />
            </div>
            
            <div className="vehicle-grid">
              {filteredVehicles.map(vehicle => (
                <div 
                  key={vehicle.variantId}
                  className={`vehicle-card ${selectedVehicle?.variantId === vehicle.variantId ? 'selected' : ''}`}
                  onClick={() => setSelectedVehicle(vehicle)}
                >
                  <div className="vehicle-image">
                    <Car size={40} />
                  </div>
                  <div className="vehicle-info">
                    <h4>{vehicle.variantName}</h4>
                    <p>{vehicle.model?.brand?.brandName} {vehicle.model?.modelName}</p>
                    <p className="price">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(vehicle.priceBase)}</p>
                    <div className="vehicle-specs">
                      <span>🔋 {vehicle.batteryCapacity}kWh</span>
                      <span>📏 {vehicle.rangeKm}km</span>
                      <span className="inventory-count">📦 {getVehicleInventoryCount(vehicle.variantId)} xe có sẵn</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Show specific vehicles from inventory when a variant is selected */}
            {selectedVehicle && availableInventory.length > 0 && (
              <div className="inventory-selection">
                <h4>Chọn xe cụ thể từ kho:</h4>
                <div className="inventory-grid">
                  {availableInventory.map(item => (
                    <div 
                      key={item.inventoryId}
                      className={`inventory-card ${selectedInventory?.inventoryId === item.inventoryId ? 'selected' : ''}`}
                      onClick={() => setSelectedInventory(item)}
                    >
                      <div className="inventory-info">
                        <h5>VIN: {item.vin}</h5>
                        <p>Chassis: {item.chassisNumber}</p>
                        <p>Màu: {item.color?.colorName}</p>
                        <p>Kho: {item.warehouse?.warehouseName}</p>
                        <p className="price">Giá: {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.sellingPrice)}</p>
                        <p>Ngày sản xuất: {new Date(item.manufacturingDate).toLocaleDateString('vi-VN')}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        );

      case 2:
        return (
          <div className="step-content">
            <h3>Chọn khách hàng</h3>
            
            {!showNewCustomerForm ? (
              <>
                <div className="search-box">
                  <Search size={20} />
                  <input
                    type="text"
                    placeholder="Tìm kiếm khách hàng..."
                    value={customerSearch}
                    onChange={(e) => setCustomerSearch(e.target.value)}
                  />
                </div>
                
                <button 
                  className="btn btn-outline-primary"
                  onClick={() => setShowNewCustomerForm(true)}
                >
                  <Plus size={16} />
                  Tạo khách hàng mới
                </button>
                
                <div className="customer-list">
                  {filteredCustomers.map(customer => (
                    <div 
                      key={customer.customerId}
                      className={`customer-card ${selectedCustomer?.customerId === customer.customerId ? 'selected' : ''}`}
                      onClick={() => setSelectedCustomer(customer)}
                    >
                      <div className="customer-avatar">
                        <User size={24} />
                      </div>
                      <div className="customer-info">
                        <h4>{customer.firstName} {customer.lastName}</h4>
                        <p>{customer.email}</p>
                        <p>{customer.phone}</p>
                        <p>{customer.address}, {customer.city}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="new-customer-form">
                <h4>Tạo khách hàng mới</h4>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Họ *</label>
                    <input
                      type="text"
                      value={newCustomer.firstName}
                      onChange={(e) => setNewCustomer({...newCustomer, firstName: e.target.value})}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Tên *</label>
                    <input
                      type="text"
                      value={newCustomer.lastName}
                      onChange={(e) => setNewCustomer({...newCustomer, lastName: e.target.value})}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Email *</label>
                    <input
                      type="email"
                      value={newCustomer.email}
                      onChange={(e) => setNewCustomer({...newCustomer, email: e.target.value})}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Số điện thoại *</label>
                    <input
                      type="tel"
                      value={newCustomer.phone}
                      onChange={(e) => setNewCustomer({...newCustomer, phone: e.target.value})}
                      required
                    />
                  </div>
                  <div className="form-group full-width">
                    <label>Địa chỉ</label>
                    <input
                      type="text"
                      value={newCustomer.address}
                      onChange={(e) => setNewCustomer({...newCustomer, address: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label>Thành phố</label>
                    <input
                      type="text"
                      value={newCustomer.city}
                      onChange={(e) => setNewCustomer({...newCustomer, city: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label>Tỉnh</label>
                    <input
                      type="text"
                      value={newCustomer.province}
                      onChange={(e) => setNewCustomer({...newCustomer, province: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label>Mã bưu điện</label>
                    <input
                      type="text"
                      value={newCustomer.postalCode}
                      onChange={(e) => setNewCustomer({...newCustomer, postalCode: e.target.value})}
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button 
                    className="btn btn-secondary"
                    onClick={() => setShowNewCustomerForm(false)}
                  >
                    Hủy
                  </button>
                  <button 
                    className="btn btn-primary"
                    onClick={handleCreateNewCustomer}
                    disabled={!newCustomer.firstName || !newCustomer.lastName || !newCustomer.email || !newCustomer.phone}
                  >
                    Tạo khách hàng
                  </button>
                </div>
              </div>
            )}
          </div>
        );

      case 3:
        return (
          <div className="step-content">
            <h3>Thông tin đơn hàng đại lý</h3>
            
            {selectedVehicle && selectedInventory && (
              <div className="order-summary">
                <div className="selected-vehicle">
                  <h4>Xe đã chọn</h4>
                  <div className="vehicle-details">
                    <Car size={24} />
                    <div>
                      <p><strong>{selectedVehicle.variantName}</strong></p>
                      <p>{selectedVehicle.model?.brand?.brandName} {selectedVehicle.model?.modelName}</p>
                      <p>Màu: {selectedInventory.color?.colorName}</p>
                      <p>VIN: {selectedInventory.vin}</p>
                    </div>
                  </div>
                </div>
                
                <div className="pricing-section">
                  <h4>Thông tin giá</h4>
                  <div className="price-breakdown">
                    <div className="price-item">
                      <span>Giá bán:</span>
                      <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateTotalPrice())}</span>
                    </div>
                    <div className="price-item">
                      <span>Tiền đặt cọc (10%):</span>
                      <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateDepositAmount())}</span>
                    </div>
                    <div className="price-item total">
                      <span>Số tiền còn lại:</span>
                      <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateTotalPrice() - calculateDepositAmount())}</span>
                    </div>
                  </div>
                </div>
                
                <div className="form-section">
                  <div className="form-group">
                    <label>Phương thức thanh toán *</label>
                    <select
                      value={orderData.paymentMethod}
                      onChange={(e) => setOrderData({...orderData, paymentMethod: e.target.value})}
                    >
                      <option value="cash">Tiền mặt</option>
                      <option value="bank_transfer">Chuyển khoản</option>
                      <option value="credit_card">Thẻ tín dụng</option>
                      <option value="installment">Trả góp</option>
                    </select>
                  </div>
                  
                  <div className="form-group">
                    <label>Số tiền đặt cọc</label>
                    <input
                      type="number"
                      value={orderData.depositAmount || calculateDepositAmount()}
                      onChange={(e) => setOrderData({...orderData, depositAmount: parseInt(e.target.value) || 0})}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>Ngày giao xe dự kiến</label>
                    <input
                      type="date"
                      value={orderData.deliveryDate}
                      onChange={(e) => setOrderData({...orderData, deliveryDate: e.target.value})}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>Ghi chú</label>
                    <textarea
                      value={orderData.notes}
                      onChange={(e) => setOrderData({...orderData, notes: e.target.value})}
                      rows={3}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>Yêu cầu đặc biệt</label>
                    <textarea
                      value={orderData.specialRequests}
                      onChange={(e) => setOrderData({...orderData, specialRequests: e.target.value})}
                      rows={2}
                    />
                  </div>
                </div>
              </div>
            )}
          </div>
        );

      case 4:
        return (
          <div className="step-content">
            <h3>Xác nhận đơn hàng đại lý</h3>
            
            <div className="confirmation-summary">
              <div className="summary-section">
                <h4>Thông tin khách hàng</h4>
                <div className="info-card">
                  <User size={20} />
                  <div>
                    <p><strong>{selectedCustomer?.firstName} {selectedCustomer?.lastName}</strong></p>
                    <p>{selectedCustomer?.email}</p>
                    <p>{selectedCustomer?.phone}</p>
                    <p>{selectedCustomer?.address}, {selectedCustomer?.city}</p>
                  </div>
                </div>
              </div>
              
              <div className="summary-section">
                <h4>Thông tin xe</h4>
                <div className="info-card">
                  <Car size={20} />
                  <div>
                    <p><strong>{selectedVehicle?.variantName}</strong></p>
                    <p>{selectedVehicle?.model?.brand?.brandName} {selectedVehicle?.model?.modelName}</p>
                    <p>Màu: {selectedInventory?.color?.colorName}</p>
                    <p>VIN: {selectedInventory?.vin}</p>
                  </div>
                </div>
              </div>
              
              <div className="summary-section">
                <h4>Thông tin đơn hàng</h4>
                <div className="info-card">
                  <FileText size={20} />
                  <div>
                    <p><strong>Tổng giá trị:</strong> {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateTotalPrice())}</p>
                    <p><strong>Tiền đặt cọc:</strong> {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(orderData.depositAmount || calculateDepositAmount())}</p>
                    <p><strong>Phương thức thanh toán:</strong> {orderData.paymentMethod}</p>
                    {orderData.deliveryDate && <p><strong>Ngày giao xe:</strong> {new Date(orderData.deliveryDate).toLocaleDateString('vi-VN')}</p>}
                    {orderData.notes && <p><strong>Ghi chú:</strong> {orderData.notes}</p>}
                  </div>
                </div>
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  const canProceedToNext = () => {
    switch (currentStep) {
      case 1:
        return selectedVehicle !== null && selectedInventory !== null;
      case 2:
        return selectedCustomer !== null;
      case 3:
        return true;
      case 4:
        return true;
      default:
        return false;
    }
  };

  if (loading && currentStep === 1) {
    return <LoadingSpinner />;
  }

  // Check if user has permission to access sales page
  const canAccessSales = hasAnyRole(['admin', 'evm_staff', 'dealer_manager', 'dealer_staff']);
  
  if (!canAccessSales) {
    return (
      <div className="unauthorized-access">
        <h2>Không có quyền truy cập</h2>
        <p>Bạn không có quyền truy cập trang bán hàng.</p>
      </div>
    );
  }

  return (
    <div className="sales-page">
      <div className="page-header">
        <h1>Đại lý mua xe</h1>
        <p>Hệ thống bán hàng cho đại lý - Tạo đơn hàng mới cho khách hàng</p>
      </div>

      <div className="sales-container">
        {/* Progress Steps */}
        <div className="steps-container">
          {steps.map((step, index) => (
            <div key={step.id} className={`step ${currentStep >= step.id ? 'active' : ''} ${currentStep > step.id ? 'completed' : ''}`}>
              <div className="step-icon">
                <step.icon size={20} />
              </div>
              <div className="step-content">
                <h4>{step.title}</h4>
                <p>{step.description}</p>
              </div>
              {index < steps.length - 1 && <div className="step-connector" />}
            </div>
          ))}
        </div>

        {/* Main Content */}
        <div className="main-content">
          {renderStepContent()}
        </div>

        {/* Navigation */}
        <div className="navigation">
          <button 
            className="btn btn-secondary"
            onClick={handlePrevStep}
            disabled={currentStep === 1}
          >
            <ArrowLeft size={16} />
            Quay lại
          </button>
          
          {currentStep < steps.length ? (
            <button 
              className="btn btn-primary"
              onClick={handleNextStep}
              disabled={!canProceedToNext()}
            >
              Tiếp theo
              <ArrowRight size={16} />
            </button>
          ) : (
            <button 
              className="btn btn-success"
              onClick={handleSubmitOrder}
              disabled={loading}
            >
              {loading ? <LoadingSpinner /> : (
                <>
                  <CheckCircle size={16} />
                  Tạo đơn hàng đại lý
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default SalesPage;
