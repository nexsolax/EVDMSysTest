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
  Shield,
  FileCheck,
  RefreshCw
} from 'lucide-react';
import { 
  publicInventoryAPI, 
  publicVehicleAPI, 
  publicCustomerAPI,
  publicOrderAPI,
  publicPaymentAPI,
  publicContractAPI,
  publicDeliveryAPI,
  publicAppointmentAPI,
  publicQuotationAPI
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
  const [quotation, setQuotation] = useState(null);
  const [contract, setContract] = useState(null);
  const [quotationLoading, setQuotationLoading] = useState(false);
  
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
    { id: 3, title: 'Tạo đơn hàng', icon: ShoppingCart, description: 'Tạo yêu cầu mua xe, status = "pending"' },
    { id: 4, title: 'Xem và xác nhận báo giá', icon: FileCheck, description: 'Xem báo giá từ nhân viên và xác nhận chấp nhận' },
    { id: 5, title: 'Thanh toán', icon: DollarSign, description: 'Thanh toán sau khi đã xác nhận báo giá' },
    { id: 6, title: 'Hoàn tất', icon: CheckCircle, description: 'Xem hợp đồng và đặt lịch giao xe' }
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
    }
  }, [selectedInventory]);

  useEffect(() => {
    // Update payment amount when quotation is loaded
    if (quotation && quotation.finalPrice) {
      setPaymentData(prev => ({
        ...prev,
        amount: quotation.finalPrice
      }));
    }
  }, [quotation]);

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
      // Theo guide line 261: firstName và lastName là required, phải có ít nhất email HOẶC phone (không bắt buộc cả 2)
      if (!customerData.firstName || !customerData.lastName) {
        toast.error('Vui lòng điền đầy đủ thông tin bắt buộc (Họ, Tên)');
        return;
      }
      if (!customerData.email && !customerData.phone) {
        toast.error('Vui lòng điền ít nhất Email hoặc Số điện thoại');
        return;
      }
      
      try {
        setLoading(true);
        
        // Required fields (theo INPUT_ORDER_GUIDE.md line 261)
        const customerPayload = {
          firstName: customerData.firstName?.trim() || '',
          lastName: customerData.lastName?.trim() || ''
        };
        
        // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
        if (customerData.email?.trim()) {
          customerPayload.email = customerData.email.trim();
        }
        if (customerData.phone?.trim()) {
          customerPayload.phone = customerData.phone.trim();
        }
        if (customerData.dateOfBirth?.trim()) {
          customerPayload.dateOfBirth = customerData.dateOfBirth.trim(); // Format: YYYY-MM-DD
        }
        if (customerData.address?.trim()) {
          customerPayload.address = customerData.address.trim();
        }
        if (customerData.city?.trim()) {
          customerPayload.city = customerData.city.trim();
        }
        if (customerData.province?.trim()) {
          customerPayload.province = customerData.province.trim();
        }
        if (customerData.postalCode?.trim()) {
          customerPayload.postalCode = customerData.postalCode.trim();
        }
        if (customerData.creditScore) {
          customerPayload.creditScore = parseInt(customerData.creditScore, 10);
        }
        if (customerData.preferredContactMethod?.trim()) {
          customerPayload.preferredContactMethod = customerData.preferredContactMethod.trim();
        }
        if (customerData.notes?.trim()) {
          customerPayload.notes = customerData.notes.trim();
        }
        
        const response = await publicCustomerAPI.createCustomer(customerPayload);
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
        // Required fields (theo INPUT_ORDER_GUIDE.md line 320-330)
        const orderPayload = {
          customerId: customer.customerId,
          inventoryId: selectedInventory.inventoryId,
          orderDate: orderData.orderDate // Format: YYYY-MM-DD
        };
        
        // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
        if (orderData.notes?.trim()) {
          orderPayload.notes = orderData.notes.trim();
        }
        
        const response = await publicOrderAPI.createOrder(orderPayload);
        const createdOrder = response.data;
        setOrder(createdOrder);
        
        // Verify order status is "pending" (lowercase)
        if (createdOrder.status && createdOrder.status !== 'pending') {
          console.warn('Order status is not "pending":', createdOrder.status);
        }
        
        toast.success(`Đơn hàng ${createdOrder.orderNumber} đã được tạo thành công!\nTrạng thái: ${createdOrder.status || 'pending'}\nNhân viên sẽ tạo báo giá cho bạn.`);
        setCurrentStep(4);
        
        // Try to load quotation if it exists (staff might have created it already)
        setTimeout(() => {
          loadQuotationForOrder(createdOrder.orderId);
        }, 1000);
      } catch (error) {
        console.error('Error creating order:', error);
        toast.error(error.response?.data?.error || 'Không thể tạo đơn hàng');
        return;
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 4) {
      // Quotation confirmation step
      // ⚠️ LƯU Ý: Không có API public để khách hàng tự xác nhận báo giá (guide line 397)
      // Khách hàng phải liên hệ trực tiếp với nhân viên để xác nhận
      if (!quotation) {
        toast.error('Chưa có báo giá. Vui lòng đợi nhân viên tạo báo giá hoặc liên hệ trực tiếp.');
        return;
      }
      
      if (quotation.status === 'accepted') {
        // Quotation already accepted by staff, proceed to payment
        setCurrentStep(5);
      } else {
        // Quotation chưa được xác nhận - yêu cầu khách hàng liên hệ nhân viên
        toast.error(
          'Báo giá chưa được xác nhận. Vui lòng liên hệ trực tiếp với nhân viên để xác nhận chấp nhận báo giá. ' +
          'Sau khi nhân viên cập nhật trạng thái, bạn có thể tiếp tục thanh toán.',
          { duration: 6000 }
        );
        return;
      }
    } else if (currentStep === 5) {
      // Payment step - proceed to final step
      setCurrentStep(6);
    } else {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const loadQuotationForOrder = async (orderId) => {
    try {
      setQuotationLoading(true);
      const response = await publicQuotationAPI.getQuotationByOrder(orderId);
      if (response.data) {
        setQuotation(response.data);
      }
    } catch (error) {
      // Quotation not found is okay, it means staff hasn't created it yet
      if (error.response?.status !== 404) {
        console.error('Error loading quotation:', error);
      }
      setQuotation(null);
    } finally {
      setQuotationLoading(false);
    }
  };

  useEffect(() => {
    // Auto-refresh quotation when on step 4 (quotation view)
    if (currentStep === 4 && order) {
      loadQuotationForOrder(order.orderId);
      
      // Set up polling to check for new quotation every 10 seconds
      const interval = setInterval(() => {
        loadQuotationForOrder(order.orderId);
      }, 10000);
      
      return () => clearInterval(interval);
    }
  }, [currentStep, order]);

  // Reload order khi quay lại step 3 để cập nhật trạng thái mới nhất
  useEffect(() => {
    const reloadOrder = async () => {
      if (currentStep === 3 && order?.orderId) {
        try {
          console.log('Reloading order to get latest status:', order.orderId);
          const response = await publicOrderAPI.getOrder(order.orderId);
          const updatedOrder = response.data;
          console.log('Order reloaded, new status:', updatedOrder.status);
          setOrder(updatedOrder);
          
          // Nếu có quotation, reload luôn
          if (updatedOrder.quotationId) {
            loadQuotationForOrder(order.orderId);
          }
        } catch (error) {
          console.error('Error reloading order:', error);
        }
      }
    };
    
    reloadOrder();
  }, [currentStep, order?.orderId]);

  const handlePayment = async () => {
    if (!order) {
      toast.error('Vui lòng tạo đơn hàng trước');
      return;
    }
    
    if (!quotation || quotation.status !== 'accepted') {
      toast.error('Vui lòng xác nhận báo giá trước khi thanh toán');
      return;
    }
    
    if (!paymentData.amount || paymentData.amount <= 0) {
      toast.error('Vui lòng nhập số tiền thanh toán');
      return;
    }
    
    try {
      setLoading(true);
      
      // Use quotation finalPrice if available, otherwise use order totalAmount
      const paymentAmount = quotation?.finalPrice || order.totalAmount || paymentData.amount;
      
      let response;
      if (paymentData.paymentType === 'deposit') {
        // Theo guide line 440-448: deposit cần orderId (required), amount (required), paymentMethod (optional), notes (optional)
        // Không có paymentDate và referenceNumber trong request body
        const depositPayload = {
          orderId: order.orderId,
          amount: Math.min(paymentData.amount, paymentAmount * 0.1),
          ...(paymentData.paymentMethod ? { paymentMethod: paymentData.paymentMethod } : {}),
          ...(paymentData.notes ? { notes: paymentData.notes } : {})
        };
        response = await publicPaymentAPI.createDeposit(depositPayload);
        toast.success('Đặt cọc thành công!');
      } else {
        // Theo guide line 462-473: full payment KHÔNG cần truyền amount (endpoint tự động lấy từ Order)
        const fullPaymentPayload = {
          orderId: order.orderId,
          ...(paymentData.paymentMethod ? { paymentMethod: paymentData.paymentMethod } : {}),
          ...(paymentData.notes ? { notes: paymentData.notes } : {})
        };
        response = await publicPaymentAPI.createFullPayment(fullPaymentPayload);
        toast.success('Thanh toán thành công!');
      }
      
      // Reload order to get updated status (should be "paid" after full payment)
      const updatedOrder = await publicOrderAPI.getOrder(order.orderId);
      setOrder(updatedOrder.data);
      
      // Check if order status changed to "paid" (lowercase)
      if (updatedOrder.data.status === 'paid') {
        toast.success('Đơn hàng đã được thanh toán đủ. Nhân viên sẽ tạo hợp đồng cho bạn.');
        
        // Try to load contract if available (staff might create it immediately)
        setTimeout(async () => {
          try {
            const contractsRes = await publicContractAPI.getContractsByOrder(order.orderId);
            if (contractsRes.data && contractsRes.data.length > 0) {
              setContract(contractsRes.data[0]);
            }
          } catch (e) {
            console.log('No contract available yet');
          }
        }, 2000);
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
      // Theo guide line 781: appointmentDate phải là LocalDateTime (Format: ISO 8601, YYYY-MM-DDTHH:mm:ss)
      // Input type="datetime-local" trả về "YYYY-MM-DDTHH:mm", cần thêm ":00" cho seconds
      let appointmentDateFormatted = appointmentData.appointmentDate;
      if (appointmentDateFormatted && !appointmentDateFormatted.includes(':')) {
        // Nếu không có time, thêm mặc định 00:00:00
        appointmentDateFormatted = `${appointmentDateFormatted}T00:00:00`;
      } else if (appointmentDateFormatted && appointmentDateFormatted.split(':').length === 2) {
        // Nếu có HH:mm nhưng không có seconds, thêm :00
        appointmentDateFormatted = `${appointmentDateFormatted}:00`;
      }
      
      // Required fields (theo INPUT_ORDER_GUIDE.md line 777-797)
      const appointmentPayload = {
        customerName: `${customer.firstName} ${customer.lastName}`.trim(), // Required
        appointmentDate: appointmentDateFormatted // Required - LocalDateTime format: YYYY-MM-DDTHH:mm:ss
      };
      
      // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
      if (customer.phone?.trim()) {
        appointmentPayload.customerPhone = customer.phone.trim();
      }
      if (customer.email?.trim()) {
        appointmentPayload.customerEmail = customer.email.trim();
      }
      if (appointmentData.notes?.trim()) {
        appointmentPayload.notes = appointmentData.notes.trim();
      }
      
      if (appointmentData.appointmentType === 'delivery') {
        if (order?.orderId) {
          appointmentPayload.orderId = order.orderId; // Required for delivery
        }
        if (appointmentData.deliveryAddress?.trim() || customer.address?.trim()) {
          appointmentPayload.deliveryAddress = (appointmentData.deliveryAddress || customer.address).trim();
        }
        const response = await publicAppointmentAPI.createDeliveryAppointment(appointmentPayload);
        toast.success('Đã đặt lịch giao xe thành công!');
      } else {
        if (selectedInventory?.variant?.variantId) {
          appointmentPayload.variantId = parseInt(selectedInventory.variant.variantId, 10); // Optional for test-drive
        }
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
        // Theo guide: firstName và lastName là required, phải có ít nhất email HOẶC phone
        return customerData.firstName && customerData.lastName && (customerData.email || customerData.phone);
      case 3:
        return customer !== null && selectedInventory !== null;
      case 4:
        // Can proceed if quotation is accepted or if we're just viewing
        return order !== null;
      case 5:
        // Can proceed to payment if quotation is accepted
        return order !== null && quotation?.status === 'accepted';
      case 6:
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
                <label htmlFor="email">Email * (hoặc Số điện thoại)</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className="form-input"
                  value={customerData.email}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  placeholder="Email hoặc số điện thoại"
                />
              </div>

              <div className="form-group">
                <label htmlFor="phone">Số điện thoại * (hoặc Email)</label>
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  className="form-input"
                  value={customerData.phone}
                  onChange={(e) => handleInputChange(e, 'customer')}
                  placeholder="Số điện thoại hoặc email"
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
            <h3>Xem và xác nhận báo giá</h3>
            
            {order && (
              <div className="order-info-card">
                <h4>Thông tin đơn hàng:</h4>
                <p><strong>Số đơn:</strong> {order.orderNumber}</p>
                <p><strong>Ngày đặt:</strong> {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                <p><strong>Trạng thái đơn hàng:</strong> <span className="badge badge-info">{order.status || 'pending'}</span></p>
              </div>
            )}

            {quotationLoading ? (
              <div className="loading-box">
                <RefreshCw size={24} className="spinning" />
                <p>Đang tải báo giá...</p>
              </div>
            ) : quotation ? (
              <div className="quotation-card">
                <div className="quotation-header">
                  <FileCheck size={24} />
                  <h4>Báo giá từ nhân viên</h4>
                </div>
                
                <div className="quotation-details">
                  <div className="detail-row">
                    <span className="label">Số báo giá:</span>
                    <span className="value">{quotation.quotationNumber || 'N/A'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Ngày tạo:</span>
                    <span className="value">
                      {quotation.quotationDate ? new Date(quotation.quotationDate).toLocaleDateString('vi-VN') : 'N/A'}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Trạng thái:</span>
                    <span className={`badge ${
                      quotation.status === 'accepted' ? 'badge-success' :
                      quotation.status === 'sent' ? 'badge-warning' :
                      quotation.status === 'pending' ? 'badge-info' :
                      'badge-secondary'
                    }`}>
                      {quotation.status === 'accepted' ? 'Đã chấp nhận' :
                       quotation.status === 'sent' ? 'Đã gửi' :
                       quotation.status === 'pending' ? 'Chờ xác nhận' :
                       quotation.status || 'N/A'}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Giá gốc:</span>
                    <span className="value">
                      {quotation.totalPrice ? `${Number(quotation.totalPrice).toLocaleString('vi-VN')} VNĐ` : 'N/A'}
                    </span>
                  </div>
                  {quotation.discountAmount && quotation.discountAmount > 0 && (
                    <div className="detail-row">
                      <span className="label">Giảm giá:</span>
                      <span className="value discount">
                        -{Number(quotation.discountAmount).toLocaleString('vi-VN')} VNĐ
                      </span>
                    </div>
                  )}
                  <div className="detail-row highlight">
                    <span className="label">Thành tiền:</span>
                    <span className="value final-price">
                      {quotation.finalPrice ? `${Number(quotation.finalPrice).toLocaleString('vi-VN')} VNĐ` : 'N/A'}
                    </span>
                  </div>
                  {quotation.validUntil && (
                    <div className="detail-row">
                      <span className="label">Có hiệu lực đến:</span>
                      <span className="value">
                        {new Date(quotation.validUntil).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                  )}
                  {quotation.notes && (
                    <div className="detail-row full-width">
                      <span className="label">Ghi chú:</span>
                      <span className="value">{quotation.notes}</span>
                    </div>
                  )}
                </div>

                {/* ⚠️ LƯU Ý: Không có API public để khách hàng tự xác nhận báo giá (guide line 397) */}
                {quotation.status === 'sent' && (
                  <div className="info-box warning">
                    <FileCheck size={20} />
                    <div>
                      <h4>Báo giá đã được gửi</h4>
                      <p>Vui lòng liên hệ trực tiếp với nhân viên để xác nhận chấp nhận báo giá.</p>
                      <p>Sau khi nhân viên cập nhật trạng thái thành "accepted", bạn có thể tiếp tục thanh toán.</p>
                      <p><strong>Liên hệ:</strong> Email: info@evdm.com | Điện thoại: 0123 456 789</p>
                    </div>
                  </div>
                )}

                {quotation.status === 'accepted' && (
                  <div className="info-box success">
                    <CheckCircle size={20} />
                    <p>Báo giá đã được xác nhận. Bạn có thể tiến hành thanh toán.</p>
                  </div>
                )}

                {quotation.status === 'pending' && (
                  <div className="info-box info">
                    <FileCheck size={20} />
                    <p>Báo giá đang chờ xử lý. Nhân viên sẽ gửi báo giá cho bạn sớm nhất.</p>
                  </div>
                )}
              </div>
            ) : (
              <div className="info-box">
                <RefreshCw size={24} />
                <div>
                  <h4>Chưa có báo giá</h4>
                  <p>Nhân viên đang xem xét đơn hàng của bạn và sẽ tạo báo giá sớm nhất.</p>
                  <p>Bạn có thể:</p>
                  <ul>
                    <li>Đợi nhân viên tạo báo giá (trang sẽ tự động cập nhật)</li>
                    <li>Liên hệ trực tiếp với nhân viên để yêu cầu báo giá</li>
                  </ul>
                  <button
                    className="btn btn-secondary"
                    onClick={() => loadQuotationForOrder(order.orderId)}
                    disabled={quotationLoading}
                  >
                    <RefreshCw size={16} />
                    {quotationLoading ? 'Đang tải...' : 'Tải lại báo giá'}
                  </button>
                </div>
              </div>
            )}

            <div className="info-box">
              <p>💡 Sau khi nhân viên tạo báo giá, bạn sẽ thấy báo giá ở đây. Vui lòng xem xét và <strong>liên hệ trực tiếp với nhân viên</strong> để xác nhận chấp nhận báo giá trước khi thanh toán.</p>
            </div>
          </div>
        );

      case 5:
        return (
          <div className="step-content">
            <h3>Thanh toán</h3>
            
            {order && (
              <div className="order-info-card">
                <h4>Thông tin đơn hàng:</h4>
                <p><strong>Số đơn:</strong> {order.orderNumber}</p>
                <p><strong>Ngày đặt:</strong> {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                <p><strong>Trạng thái:</strong> <span className="badge badge-info">{order.status || 'pending'}</span></p>
                {quotation && (
                  <p><strong>Giá báo giá:</strong> {quotation.finalPrice ? `${Number(quotation.finalPrice).toLocaleString('vi-VN')} VNĐ` : 'N/A'}</p>
                )}
              </div>
            )}

            {quotation && quotation.status === 'accepted' && (
              <div className="info-box success">
                <CheckCircle size={20} />
                <p>Bạn đã xác nhận báo giá. Vui lòng thanh toán để hoàn tất đơn hàng.</p>
              </div>
            )}

            {(!quotation || quotation.status !== 'accepted') && (
              <div className="info-box warning">
                <p>⚠️ Vui lòng xác nhận báo giá trước khi thanh toán.</p>
              </div>
            )}

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
                    if (quotation) {
                      const amount = e.target.value === 'deposit' 
                        ? (quotation.finalPrice || 0) * 0.1 
                        : (quotation.finalPrice || 0);
                      setPaymentData(prev => ({ ...prev, amount }));
                    } else if (order) {
                      const amount = e.target.value === 'deposit' 
                        ? (order.totalAmount || 0) * 0.1 
                        : (order.totalAmount || 0);
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
              disabled={loading || !order || !quotation || quotation.status !== 'accepted'}
            >
              <DollarSign size={20} />
              {loading ? 'Đang xử lý...' : 'Thanh toán'}
            </button>
          </div>
        );

      case 6:
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
                    <p><strong>Trạng thái:</strong> {contract.contractStatus || contract.status || 'draft'}</p>
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

