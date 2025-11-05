import React, { useState, useEffect } from 'react';
import { 
  Car, 
  User, 
  ShoppingCart, 
  DollarSign, 
  FileText,
  Truck,
  CheckCircle,
  ArrowRight,
  ArrowLeft,
  Search,
  Plus,
  Calendar,
  Eye,
  Zap,
  Shield
} from 'lucide-react';
import { 
  publicInventoryAPI, 
  publicVehicleAPI, 
  publicCustomerAPI,
  publicOrderAPI,
  publicPaymentAPI,
  publicContractAPI,
  publicDeliveryAPI,
  publicAppointmentAPI
} from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import VehicleImage from '../components/VehicleImage';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './PublicPurchaseFlow.css';

const PublicPurchaseFlow = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  
  // Data states
  const [vehicles, setVehicles] = useState([]);
  const [brands, setBrands] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [selectedInventory, setSelectedInventory] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [order, setOrder] = useState(null);
  const [contract, setContract] = useState(null);
  
  // Form states
  const [customerData, setCustomerData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    creditScore: null,
    preferredContactMethod: 'phone',
    notes: ''
  });
  
  const [orderData, setOrderData] = useState({
    orderDate: new Date().toISOString().split('T')[0],
    orderType: 'RETAIL',
    paymentStatus: 'PENDING',
    deliveryStatus: 'PENDING',
    totalAmount: 0,
    depositAmount: 0,
    balanceAmount: 0,
    paymentMethod: 'bank_transfer',
    notes: '',
    specialRequests: ''
  });
  
  const [paymentData, setPaymentData] = useState({
    paymentType: 'full', // 'deposit' or 'full'
    amount: 0,
    paymentMethod: 'bank_transfer',
    referenceNumber: '',
    notes: ''
  });
  
  const [appointmentData, setAppointmentData] = useState({
    appointmentType: 'delivery', // 'test-drive' or 'delivery'
    appointmentDate: '',
    deliveryAddress: '',
    notes: ''
  });
  
  // Search states
  const [vehicleSearch, setVehicleSearch] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('');

  const steps = [
    { id: 1, title: 'Xem và chọn xe', icon: Car, description: 'Xem danh sách xe và chọn xe bạn muốn mua' },
    { id: 2, title: 'Thông tin khách hàng', icon: User, description: 'Điền thông tin cá nhân để tạo tài khoản' },
    { id: 3, title: 'Tạo đơn hàng', icon: ShoppingCart, description: 'Tạo yêu cầu mua xe và chờ báo giá' },
    { id: 4, title: 'Thanh toán', icon: DollarSign, description: 'Thanh toán sau khi nhận báo giá từ nhân viên' },
    { id: 5, title: 'Hoàn tất', icon: CheckCircle, description: 'Ký hợp đồng và đặt lịch giao xe' }
  ];

  // Enrich inventory data with missing relationships
  // This function fetches variant, model, and brand data if they're missing from API response
  const enrichInventoryData = async (inventoryList) => {
    if (!inventoryList || inventoryList.length === 0) return inventoryList;

    try {
      // Fetch all variants, models, and brands in parallel using public API
      const [variantsRes, modelsRes, brandsRes] = await Promise.all([
        publicVehicleAPI.getVariants().catch(() => ({ data: [] })),
        publicVehicleAPI.getModels().catch(() => ({ data: [] })),
        publicVehicleAPI.getBrands().catch(() => ({ data: [] }))
      ]);

      const variants = variantsRes.data || [];
      const models = modelsRes.data || [];
      const brands = brandsRes.data || [];

      // Create lookup maps for faster access
      // Use both string and number keys to handle type mismatches
      const variantMap = new Map();
      variants.forEach(v => {
        variantMap.set(v.variantId, v);
        variantMap.set(String(v.variantId), v);
        variantMap.set(Number(v.variantId), v);
      });
      
      const modelMap = new Map();
      models.forEach(m => {
        modelMap.set(m.modelId, m);
        modelMap.set(String(m.modelId), m);
        modelMap.set(Number(m.modelId), m);
      });
      
      const brandMap = new Map();
      brands.forEach(b => {
        brandMap.set(b.brandId, b);
        brandMap.set(String(b.brandId), b);
        brandMap.set(Number(b.brandId), b);
      });

      // Enrich each inventory item
      return inventoryList.map(item => {
        const enriched = { ...item };

        // If variant is missing but variantId exists, fetch from map
        // Also enrich if variant exists but is missing image data
        if (!enriched.variant && enriched.variantId) {
          const variant = variantMap.get(enriched.variantId) || variantMap.get(String(enriched.variantId)) || variantMap.get(Number(enriched.variantId));
          if (variant) {
            enriched.variant = variant;
          }
        } else if (enriched.variant && enriched.variantId && (!enriched.variant.variantImageUrl && !enriched.variant.variantImagePath)) {
          // Variant exists but missing image, try to enrich from map
          const variant = variantMap.get(enriched.variantId) || variantMap.get(String(enriched.variantId)) || variantMap.get(Number(enriched.variantId));
          if (variant && (variant.variantImageUrl || variant.variantImagePath)) {
            enriched.variant = { ...enriched.variant, ...variant };
          }
        }

        // If variant exists but model is missing, enrich it
        if (enriched.variant && !enriched.variant.model && enriched.variant.modelId) {
          const model = modelMap.get(enriched.variant.modelId);
          if (model) {
            enriched.variant.model = model;
            
            // If model exists but brand is missing, enrich it
            if (!model.brand && model.brandId) {
              const brand = brandMap.get(model.brandId);
              if (brand) {
                enriched.variant.model.brand = brand;
              }
            }
          }
        }

        return enriched;
      });
    } catch (error) {
      console.error('Error enriching inventory data:', error);
      // Return original data if enrichment fails
      return inventoryList;
    }
  };

  useEffect(() => {
    const loadData = async () => {
      // Check if there's inventoryId in URL params
      const urlParams = new URLSearchParams(window.location.search);
      const inventoryId = urlParams.get('inventoryId');
      
      await loadInitialData();
      
      if (inventoryId) {
        // Load specific inventory item
        try {
          const res = await publicInventoryAPI.getInventory();
          let inventoryData = res.data || [];
          
          // Enrich inventory data
          inventoryData = await enrichInventoryData(inventoryData);
          
          const inventory = inventoryData.find(item => item.inventoryId === inventoryId);
          if (inventory && inventory.status === 'available') {
            setSelectedInventory(inventory);
            setSelectedVehicle(inventory.variant);
          }
        } catch (err) {
          console.error('Error loading inventory:', err);
        }
      }
    };
    
    loadData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (selectedInventory) {
      const price = selectedInventory.sellingPrice || selectedInventory.variant?.priceBase || 0;
      setOrderData(prev => ({
        ...prev,
        totalAmount: price,
        depositAmount: Math.round(price * 0.1),
        balanceAmount: Math.round(price * 0.9)
      }));
      setPaymentData(prev => ({
        ...prev,
        amount: price
      }));
    }
  }, [selectedInventory]);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [inventoryRes, brandsRes] = await Promise.all([
        publicInventoryAPI.getInventory().catch(() => ({ data: [] })),
        publicVehicleAPI.getBrands().catch(() => ({ data: [] }))
      ]);
      
      let inventoryData = inventoryRes.data || [];
      
      // Enrich inventory data with variant, model, and brand information
      inventoryData = await enrichInventoryData(inventoryData);
      
      // Filter available vehicles
      const availableVehicles = inventoryData.filter(v => v.status === 'available');
      setVehicles(availableVehicles);
      setBrands(brandsRes.data || []);
    } catch (error) {
      console.error('Error loading initial data:', error);
      toast.error('Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e, formType) => {
    const { name, value } = e.target;
    switch (formType) {
      case 'customer':
        setCustomerData(prev => ({ ...prev, [name]: value }));
        break;
      case 'order':
        setOrderData(prev => ({ ...prev, [name]: value }));
        break;
      case 'payment':
        setPaymentData(prev => ({ ...prev, [name]: value }));
        break;
      case 'appointment':
        setAppointmentData(prev => ({ ...prev, [name]: value }));
        break;
      default:
        break;
    }
  };

  const handleNextStep = async () => {
    if (currentStep === 1) {
      if (!selectedInventory) {
        toast.error('Vui lòng chọn xe để mua');
        return;
      }
      setCurrentStep(2);
    } else if (currentStep === 2) {
      // Validate customer data
      if (!customerData.firstName || !customerData.lastName || !customerData.email || !customerData.phone) {
        toast.error('Vui lòng điền đầy đủ thông tin bắt buộc (Họ, Tên, Email, Số điện thoại)');
        return;
      }
      
      try {
        setLoading(true);
        const response = await publicCustomerAPI.createCustomer(customerData);
        setCustomer(response.data);
        toast.success('Đã tạo thông tin khách hàng thành công');
        setCurrentStep(3);
      } catch (error) {
        console.error('Error creating customer:', error);
        toast.error(error.response?.data?.error || 'Không thể tạo thông tin khách hàng');
        return;
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 3) {
      if (!customer) {
        toast.error('Vui lòng tạo thông tin khách hàng trước');
        return;
      }
      
      try {
        setLoading(true);
        const orderPayload = {
          customerId: customer.customerId,
          inventoryId: selectedInventory.inventoryId,
          orderDate: orderData.orderDate,
          orderType: orderData.orderType,
          paymentStatus: orderData.paymentStatus,
          deliveryStatus: orderData.deliveryStatus,
          totalAmount: orderData.totalAmount,
          depositAmount: orderData.depositAmount,
          balanceAmount: orderData.balanceAmount,
          paymentMethod: orderData.paymentMethod,
          notes: orderData.notes || null,
          specialRequests: orderData.specialRequests || null
        };
        
        const response = await publicOrderAPI.createOrder(orderPayload);
        setOrder(response.data);
        toast.success(`Đơn hàng ${response.data.orderNumber} đã được tạo thành công!\nNhân viên sẽ liên hệ với bạn để báo giá.`);
        setCurrentStep(4);
      } catch (error) {
        console.error('Error creating order:', error);
        toast.error(error.response?.data?.error || 'Không thể tạo đơn hàng');
        return;
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 4) {
      // Payment step - user can proceed without payment for now
      // They will receive quotation and can pay later
      setCurrentStep(5);
    } else {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handlePayment = async () => {
    if (!order) {
      toast.error('Vui lòng tạo đơn hàng trước');
      return;
    }
    
    if (!paymentData.amount || paymentData.amount <= 0) {
      toast.error('Vui lòng nhập số tiền thanh toán');
      return;
    }
    
    try {
      setLoading(true);
      const paymentPayload = {
        orderId: order.orderId,
        amount: paymentData.amount,
        paymentType: paymentData.paymentType,
        paymentMethod: paymentData.paymentMethod,
        referenceNumber: paymentData.referenceNumber || `TXN-${Date.now()}`,
        notes: paymentData.notes || null
      };
      
      let response;
      if (paymentData.paymentType === 'deposit') {
        response = await publicPaymentAPI.createDeposit(paymentPayload);
      } else {
        response = await publicPaymentAPI.createFullPayment(paymentPayload);
      }
      toast.success('Thanh toán thành công!');
      
      // Reload order to get updated status
      const updatedOrder = await publicOrderAPI.getOrder(order.orderId);
      setOrder(updatedOrder.data);
      
      // Try to load contract if available
      try {
        const contractsRes = await publicContractAPI.getContractsByOrder(order.orderId);
        if (contractsRes.data && contractsRes.data.length > 0) {
          setContract(contractsRes.data[0]);
        }
      } catch (e) {
        console.log('No contract available yet');
      }
      
    } catch (error) {
      console.error('Error processing payment:', error);
      toast.error(error.response?.data?.error || 'Không thể xử lý thanh toán');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAppointment = async () => {
    if (!customer || !appointmentData.appointmentDate) {
      toast.error('Vui lòng điền đầy đủ thông tin');
      return;
    }
    
    try {
      setLoading(true);
      const appointmentPayload = {
        customerName: `${customer.firstName} ${customer.lastName}`,
        customerPhone: customer.phone,
        customerEmail: customer.email,
        appointmentDate: appointmentData.appointmentDate,
        notes: appointmentData.notes || null
      };
      
      if (appointmentData.appointmentType === 'delivery') {
        appointmentPayload.orderId = order?.orderId;
        appointmentPayload.deliveryAddress = appointmentData.deliveryAddress || customer.address;
        const response = await publicAppointmentAPI.createDeliveryAppointment(appointmentPayload);
        toast.success('Đã đặt lịch giao xe thành công!');
      } else {
        appointmentPayload.variantId = selectedInventory?.variant?.variantId;
        const response = await publicAppointmentAPI.createTestDriveAppointment(appointmentPayload);
        toast.success('Đã đặt lịch lái thử thành công!');
      }
    } catch (error) {
      console.error('Error creating appointment:', error);
      toast.error(error.response?.data?.error || 'Không thể tạo lịch hẹn');
    } finally {
      setLoading(false);
    }
  };

  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesSearch = (vehicle.variant?.variantName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase()) ||
                         (vehicle.variant?.model?.modelName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase()) ||
                         (vehicle.variant?.model?.brand?.brandName?.toLowerCase() || '').includes(vehicleSearch.toLowerCase());
    
    const matchesBrand = !selectedBrand || vehicle.variant?.model?.brand?.brandName === selectedBrand;
    
    return matchesSearch && matchesBrand && vehicle.status === 'available';
  });

  const canProceedToNext = () => {
    switch (currentStep) {
      case 1:
        return selectedInventory !== null;
      case 2:
        return customerData.firstName && customerData.lastName && customerData.email && customerData.phone;
      case 3:
        return customer !== null && selectedInventory !== null;
      case 4:
        return order !== null;
      case 5:
        return true;
      default:
        return false;
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="step-content">
            <h3>Chọn xe bạn muốn mua</h3>
            
            <div className="search-box">
              <Search size={20} />
              <input
                type="text"
                placeholder="Tìm kiếm xe theo tên, model, thương hiệu..."
                value={vehicleSearch}
                onChange={(e) => setVehicleSearch(e.target.value)}
              />
            </div>

            {brands.length > 0 && (
              <div className="brand-filter">
                <label>Lọc theo thương hiệu:</label>
                <select
                  value={selectedBrand}
                  onChange={(e) => setSelectedBrand(e.target.value)}
                  className="form-select"
                >
                  <option value="">Tất cả</option>
                  {brands.map((brand) => (
                    <option key={brand.brandId} value={brand.brandName}>
                      {brand.brandName}
                    </option>
                  ))}
                </select>
              </div>
            )}
            
            <div className="vehicle-grid">
              {filteredVehicles.map((vehicle) => {
                const isSelected = selectedInventory?.inventoryId === vehicle.inventoryId;
                return (
                  <div
                    key={vehicle.inventoryId}
                    className={`vehicle-card ${isSelected ? 'selected' : ''}`}
                    onClick={() => {
                      setSelectedInventory(vehicle);
                      setSelectedVehicle(vehicle.variant);
                    }}
                  >
                    <div className="vehicle-image">
                      <VehicleImage vehicle={vehicle} size={48} />
                    </div>
                    <div className="vehicle-info">
                      <h4>{vehicle.variant?.variantName || 'N/A'}</h4>
                      <p>{vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName}</p>
                      <div className="vehicle-specs">
                        <span><Zap size={14} /> {vehicle.variant?.powerKw || 'N/A'} kW</span>
                        <span><Car size={14} /> {vehicle.variant?.rangeKm || 'N/A'} km</span>
                        <span><Shield size={14} /> {vehicle.variant?.batteryCapacity || 'N/A'} kWh</span>
                      </div>
                      <div className="vehicle-price">
                        {(vehicle.sellingPrice || vehicle.variant?.priceBase || 0).toLocaleString('vi-VN')} VNĐ
                      </div>
                      {isSelected && (
                        <div className="selected-badge">
                          <CheckCircle size={16} />
                          Đã chọn
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
            
            {filteredVehicles.length === 0 && (
              <div className="empty-state">
                <p>Không tìm thấy xe phù hợp</p>
              </div>
            )}
          </div>
        );

      case 2:
        return (
          <div className="step-content">
            <h3>Thông tin khách hàng</h3>
            <p className="step-description">Vui lòng điền thông tin để tạo tài khoản khách hàng</p>
            
            <div className="form-grid">
              <div className="form-group">
                <label htmlFor="firstName">Họ *</label>
                <input
                  id="firstName"
                  name="firstName"
                  type="text"
                  className="form-input"
                  value={customerData.firstName}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Tên *</label>
                <input
                  id="lastName"
                  name="lastName"
                  type="text"
                  className="form-input"
                  value={customerData.lastName}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Email *</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className="form-input"
                  value={customerData.email}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="phone">Số điện thoại *</label>
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  className="form-input"
                  value={customerData.phone}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="dateOfBirth">Ngày sinh</label>
                <input
                  id="dateOfBirth"
                  name="dateOfBirth"
                  type="date"
                  className="form-input"
                  value={customerData.dateOfBirth}
                  onChange={(e) => handleInputChange(e, 'customer')}
                />
              </div>

              <div className="form-group full-width">
                <label htmlFor="address">Địa chỉ</label>
                <input
                  id="address"
                  name="address"
                  type="text"
                  className="form-input"
                  value={customerData.address}
                  onChange={(e) => handleInputChange(e, 'customer')}
                />
              </div>

              <div className="form-group">
                <label htmlFor="city">Thành phố</label>
                <input
                  id="city"
                  name="city"
                  type="text"
                  className="form-input"
                  value={customerData.city}
                  onChange={(e) => handleInputChange(e, 'customer')}
                />
              </div>

              <div className="form-group">
                <label htmlFor="province">Tỉnh</label>
                <input
                  id="province"
                  name="province"
                  type="text"
                  className="form-input"
                  value={customerData.province}
                  onChange={(e) => handleInputChange(e, 'customer')}
                />
              </div>

              <div className="form-group">
                <label htmlFor="postalCode">Mã bưu điện</label>
                <input
                  id="postalCode"
                  name="postalCode"
                  type="text"
                  className="form-input"
                  value={customerData.postalCode}
                  onChange={(e) => handleInputChange(e, 'customer')}
                />
              </div>

              <div className="form-group full-width">
                <label htmlFor="notes">Ghi chú</label>
                <textarea
                  id="notes"
                  name="notes"
                  className="form-input"
                  rows={3}
                  value={customerData.notes}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  placeholder="Ghi chú về nhu cầu của bạn..."
                />
              </div>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="step-content">
            <h3>Thông tin đơn hàng</h3>
            
            {selectedInventory && (
              <div className="selected-vehicle-summary">
                <h4>Xe đã chọn:</h4>
                <div className="summary-card">
                  <VehicleImage vehicle={selectedInventory} size={32} />
                  <div>
                    <p><strong>{selectedInventory.variant?.variantName}</strong></p>
                    <p>{selectedInventory.variant?.model?.brand?.brandName} {selectedInventory.variant?.model?.modelName}</p>
                    <p className="price">Giá: {orderData.totalAmount.toLocaleString('vi-VN')} VNĐ</p>
                  </div>
                </div>
              </div>
            )}

            <div className="form-grid">
              <div className="form-group">
                <label htmlFor="orderDate">Ngày đặt hàng *</label>
                <input
                  id="orderDate"
                  name="orderDate"
                  type="date"
                  className="form-input"
                  value={orderData.orderDate}
                  onChange={(e) => handleInputChange(e, 'order')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="paymentMethod">Phương thức thanh toán</label>
                <select
                  id="paymentMethod"
                  name="paymentMethod"
                  className="form-select"
                  value={orderData.paymentMethod}
                  onChange={(e) => handleInputChange(e, 'order')}
                >
                  <option value="bank_transfer">Chuyển khoản</option>
                  <option value="credit_card">Thẻ tín dụng</option>
                  <option value="cash">Tiền mặt</option>
                </select>
              </div>

              <div className="form-group full-width">
                <label htmlFor="notes">Ghi chú</label>
                <textarea
                  id="notes"
                  name="notes"
                  className="form-input"
                  rows={3}
                  value={orderData.notes}
                  onChange={(e) => handleInputChange(e, 'order')}
                  placeholder="Ghi chú về đơn hàng..."
                />
              </div>

              <div className="form-group full-width">
                <label htmlFor="specialRequests">Yêu cầu đặc biệt</label>
                <textarea
                  id="specialRequests"
                  name="specialRequests"
                  className="form-input"
                  rows={3}
                  value={orderData.specialRequests}
                  onChange={(e) => handleInputChange(e, 'order')}
                  placeholder="Yêu cầu đặc biệt về giao hàng..."
                />
              </div>
            </div>

            <div className="order-summary">
              <h4>Tóm tắt đơn hàng:</h4>
              <div className="summary-item">
                <span>Tổng tiền:</span>
                <strong>{orderData.totalAmount.toLocaleString('vi-VN')} VNĐ</strong>
              </div>
              <div className="summary-item">
                <span>Đặt cọc (10%):</span>
                <strong>{orderData.depositAmount.toLocaleString('vi-VN')} VNĐ</strong>
              </div>
              <div className="summary-item">
                <span>Còn lại:</span>
                <strong>{orderData.balanceAmount.toLocaleString('vi-VN')} VNĐ</strong>
              </div>
            </div>

            <div className="info-box">
              <p>💡 Sau khi tạo đơn hàng, nhân viên sẽ liên hệ với bạn để báo giá và xác nhận đơn hàng.</p>
            </div>
          </div>
        );

      case 4:
        return (
          <div className="step-content">
            <h3>Thanh toán</h3>
            
            {order && (
              <div className="order-info-card">
                <h4>Thông tin đơn hàng:</h4>
                <p><strong>Số đơn:</strong> {order.orderNumber}</p>
                <p><strong>Ngày đặt:</strong> {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                <p><strong>Trạng thái:</strong> {order.status || 'pending'}</p>
                <p><strong>Tổng tiền:</strong> {order.totalAmount ? `${Number(order.totalAmount).toLocaleString('vi-VN')} VNĐ` : 'N/A'}</p>
              </div>
            )}

            <div className="info-box">
              <p>📧 Bạn sẽ nhận được báo giá từ nhân viên qua email hoặc điện thoại. Sau khi nhận báo giá, bạn có thể thanh toán tại đây.</p>
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label htmlFor="paymentType">Loại thanh toán</label>
                <select
                  id="paymentType"
                  name="paymentType"
                  className="form-select"
                  value={paymentData.paymentType}
                  onChange={(e) => {
                    handleInputChange(e, 'payment');
                    if (order) {
                      const amount = e.target.value === 'deposit' 
                        ? order.totalAmount * 0.1 
                        : order.totalAmount;
                      setPaymentData(prev => ({ ...prev, amount }));
                    }
                  }}
                >
                  <option value="deposit">Đặt cọc (10%)</option>
                  <option value="full">Thanh toán toàn bộ</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="paymentAmount">Số tiền (VNĐ) *</label>
                <input
                  id="paymentAmount"
                  name="amount"
                  type="number"
                  min="0"
                  step="1000"
                  className="form-input"
                  value={paymentData.amount}
                  onChange={(e) => handleInputChange(e, 'payment')}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="paymentMethod">Phương thức thanh toán</label>
                <select
                  id="paymentMethod"
                  name="paymentMethod"
                  className="form-select"
                  value={paymentData.paymentMethod}
                  onChange={(e) => handleInputChange(e, 'payment')}
                >
                  <option value="bank_transfer">Chuyển khoản</option>
                  <option value="credit_card">Thẻ tín dụng</option>
                  <option value="cash">Tiền mặt</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="referenceNumber">Số tham chiếu</label>
                <input
                  id="referenceNumber"
                  name="referenceNumber"
                  type="text"
                  className="form-input"
                  value={paymentData.referenceNumber}
                  onChange={(e) => handleInputChange(e, 'payment')}
                  placeholder="TXN-..."
                />
              </div>

              <div className="form-group full-width">
                <label htmlFor="paymentNotes">Ghi chú</label>
                <textarea
                  id="paymentNotes"
                  name="notes"
                  className="form-input"
                  rows={3}
                  value={paymentData.notes}
                  onChange={(e) => handleInputChange(e, 'payment')}
                  placeholder="Ghi chú về thanh toán..."
                />
              </div>
            </div>

            <button
              className="btn btn-primary btn-lg"
              onClick={handlePayment}
              disabled={loading || !order}
            >
              <DollarSign size={20} />
              {loading ? 'Đang xử lý...' : 'Thanh toán'}
            </button>
          </div>
        );

      case 5:
        return (
          <div className="step-content">
            <h3>Hoàn tất đơn hàng</h3>
            
            {order && (
              <div className="success-summary">
                <div className="success-icon">
                  <CheckCircle size={48} />
                </div>
                <h4>Đơn hàng đã được tạo thành công!</h4>
                <p className="order-number">Số đơn hàng: <strong>{order.orderNumber}</strong></p>
                <p>Nhân viên sẽ liên hệ với bạn trong thời gian sớm nhất để báo giá và xác nhận đơn hàng.</p>
              </div>
            )}

            {contract && (
              <div className="contract-section">
                <h4>Hợp đồng</h4>
                <div className="info-card">
                  <FileText size={24} />
                  <div>
                    <p><strong>Số hợp đồng:</strong> {contract.contractNumber}</p>
                    <p><strong>Ngày tạo:</strong> {contract.contractDate ? new Date(contract.contractDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                    <p><strong>Trạng thái:</strong> {contract.contractStatus || 'draft'}</p>
                    <button
                      className="btn btn-secondary"
                      onClick={async () => {
                        try {
                          const response = await publicContractAPI.downloadContract(contract.contractId);
                          // Handle PDF download
                          window.open(response.config.url);
                        } catch (error) {
                          toast.error('Không thể tải hợp đồng');
                        }
                      }}
                    >
                      Tải hợp đồng PDF
                    </button>
                  </div>
                </div>
              </div>
            )}

            <div className="appointment-section">
              <h4>Đặt lịch hẹn</h4>
              
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="appointmentType">Loại lịch hẹn</label>
                  <select
                    id="appointmentType"
                    name="appointmentType"
                    className="form-select"
                    value={appointmentData.appointmentType}
                    onChange={(e) => handleInputChange(e, 'appointment')}
                  >
                    <option value="test-drive">Lái thử</option>
                    <option value="delivery">Giao xe</option>
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="appointmentDate">Ngày giờ hẹn *</label>
                  <input
                    id="appointmentDate"
                    name="appointmentDate"
                    type="datetime-local"
                    className="form-input"
                    value={appointmentData.appointmentDate}
                    onChange={(e) => handleInputChange(e, 'appointment')}
                    required
                  />
                </div>

                {appointmentData.appointmentType === 'delivery' && (
                  <div className="form-group full-width">
                    <label htmlFor="deliveryAddress">Địa chỉ giao hàng</label>
                    <input
                      id="deliveryAddress"
                      name="deliveryAddress"
                      type="text"
                      className="form-input"
                      value={appointmentData.deliveryAddress}
                      onChange={(e) => handleInputChange(e, 'appointment')}
                      placeholder={customer?.address || 'Nhập địa chỉ giao hàng...'}
                    />
                  </div>
                )}

                <div className="form-group full-width">
                  <label htmlFor="appointmentNotes">Ghi chú</label>
                  <textarea
                    id="appointmentNotes"
                    name="notes"
                    className="form-input"
                    rows={3}
                    value={appointmentData.notes}
                    onChange={(e) => handleInputChange(e, 'appointment')}
                    placeholder="Ghi chú về lịch hẹn..."
                  />
                </div>
              </div>

              <button
                className="btn btn-primary"
                onClick={handleCreateAppointment}
                disabled={loading || !appointmentData.appointmentDate}
              >
                <Calendar size={20} />
                {loading ? 'Đang tạo...' : 'Đặt lịch hẹn'}
              </button>
            </div>

            <div className="order-tracking">
              <h4>Theo dõi đơn hàng</h4>
              {order && (
                <div className="info-box">
                  <p>Bạn có thể theo dõi đơn hàng bằng số đơn hàng: <strong>{order.orderNumber}</strong></p>
                  <button
                    className="btn btn-outline"
                    onClick={() => {
                      // Navigate to tracking page or show tracking info
                      toast.info('Tính năng theo dõi đơn hàng sẽ được cập nhật sớm');
                    }}
                  >
                    <Eye size={16} />
                    Xem chi tiết đơn hàng
                  </button>
                </div>
              )}
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  if (loading && currentStep === 1) {
    return <LoadingSpinner text="Đang tải dữ liệu..." />;
  }

  return (
    <div className="public-purchase-flow">
      <div className="page-header">
        <h1>Mua xe điện</h1>
        <p>Quy trình mua xe đơn giản và tiện lợi - Chỉ cần 5 bước</p>
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
              disabled={!canProceedToNext() || loading}
            >
              {loading ? <LoadingSpinner /> : (
                <>
                  Tiếp theo
                  <ArrowRight size={16} />
                </>
              )}
            </button>
          ) : (
            <button 
              className="btn btn-success"
              onClick={() => {
                toast.success('Cảm ơn bạn đã sử dụng dịch vụ!');
                // Reset or navigate
                window.location.href = '/';
              }}
            >
              <CheckCircle size={16} />
              Hoàn tất
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PublicPurchaseFlow;

