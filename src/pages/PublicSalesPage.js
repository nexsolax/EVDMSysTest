import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { 
  Car, 
  Search, 
  Filter, 
  Star, 
  Phone, 
  Mail, 
  MapPin, 
  Calendar,
  Battery,
  Gauge,
  Zap,
  Users,
  Scale,
  Ruler,
  DollarSign,
  ChevronDown,
  ChevronUp,
  Eye,
  Heart,
  Share2,
  Clock,
  LogIn
} from 'lucide-react';
import { 
  publicVehicleAPI, 
  publicInventoryAPI,
  publicCustomerAPI,
  publicQuotationAPI,
  publicOrderAPI,
  publicPromotionAPI
} from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import './PublicSalesPage.css';

const PublicSalesPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [vehicles, setVehicles] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [brands, setBrands] = useState([]);
  const [colors, setColors] = useState([]);
  const [promotions, setPromotions] = useState([]);
  
  // Filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('all');
  const [selectedColor, setSelectedColor] = useState('all');
  const [priceRange, setPriceRange] = useState({ min: 0, max: 5000000000 });
  const [sortBy, setSortBy] = useState('name');
  const [showFilters, setShowFilters] = useState(false);
  
  // Contact form states
  const [showContactForm, setShowContactForm] = useState(false);
  const [showVehicleDetail, setShowVehicleDetail] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [contactForm, setContactForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    message: '',
    interestedVehicle: '',
    wantTestDrive: false,
    wantFinancing: false,
    wantInsurance: false,
    wantAccessories: false
  });

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      console.log('Loading initial data...');
      
      // Load core data first
      const [vehiclesRes, inventoryRes, brandsRes, colorsRes] = await Promise.all([
        publicVehicleAPI.getVariants(),
        publicInventoryAPI.getInventoryByStatus('available'),
        publicVehicleAPI.getBrands(),
        publicVehicleAPI.getColors()
      ]);
      
      console.log('Core API Responses:', {
        vehicles: vehiclesRes.data,
        inventory: inventoryRes.data,
        brands: brandsRes.data,
        colors: colorsRes.data
      });
      
      setVehicles(vehiclesRes.data || []);
      setInventory(inventoryRes.data || []);
      setBrands(brandsRes.data || []);
      setColors(colorsRes.data || []);
      
      // Try to load promotions separately (non-blocking)
      try {
        const promotionsRes = await publicPromotionAPI.getPromotions();
        console.log('Promotions API Response:', promotionsRes.data);
        
        // Filter active promotions
        const allPromotions = promotionsRes.data || [];
        const activePromotions = allPromotions.filter(promotion => {
          const now = new Date();
          const startDate = new Date(promotion.startDate);
          const endDate = new Date(promotion.endDate);
          return promotion.isActive && startDate <= now && endDate >= now;
        });
        setPromotions(activePromotions);
      } catch (promotionError) {
        console.warn('Promotions API failed, continuing without promotions:', promotionError);
        setPromotions([]);
      }
      
    } catch (error) {
      console.error('Error loading initial data:', error);
      console.error('Error details:', error.response?.data);
      toast.error('Không thể tải dữ liệu xe');
      
      // Set empty arrays as fallback
      setVehicles([]);
      setInventory([]);
      setBrands([]);
      setColors([]);
      setPromotions([]);
    } finally {
      setLoading(false);
    }
  };

  // Get available vehicles with inventory count
  const getAvailableVehicles = () => {
    console.log('Processing vehicles:', { vehicles, inventory });
    
    // If no inventory, show all vehicles with default info
    if (!inventory || inventory.length === 0) {
      console.log('No inventory found, showing all vehicles');
      return vehicles.map(vehicle => ({
        ...vehicle,
        availableCount: 0,
        minPrice: vehicle.priceBase || 0,
        maxPrice: vehicle.priceBase || 0,
        colors: ['Liên hệ để biết màu sắc'],
        inventoryItems: []
      }));
    }
    
    const vehicleMap = new Map();
    
    // First, add all vehicles from variants
    vehicles.forEach(vehicle => {
      vehicleMap.set(vehicle.variantId, {
        ...vehicle,
        availableCount: 0,
        minPrice: vehicle.priceBase || 0,
        maxPrice: vehicle.priceBase || 0,
        colors: new Set(),
        inventoryItems: []
      });
    });
    
    // Then, update with inventory data
    inventory.forEach(item => {
      if (item.variant && item.status === 'available') {
        const variantId = item.variant.variantId;
        if (vehicleMap.has(variantId)) {
          const vehicle = vehicleMap.get(variantId);
          vehicle.availableCount++;
          vehicle.minPrice = Math.min(vehicle.minPrice, item.sellingPrice || vehicle.priceBase || 0);
          vehicle.maxPrice = Math.max(vehicle.maxPrice, item.sellingPrice || vehicle.priceBase || 0);
          if (item.color) {
            vehicle.colors.add(item.color.colorName);
          }
          vehicle.inventoryItems.push(item);
        }
      }
    });
    
    const result = Array.from(vehicleMap.values()).map(vehicle => ({
      ...vehicle,
      colors: vehicle.colors.size > 0 ? Array.from(vehicle.colors) : ['Liên hệ để biết màu sắc']
    }));
    
    console.log('Processed vehicles result:', result);
    return result;
  };

  const availableVehicles = getAvailableVehicles();
  console.log('Available vehicles:', availableVehicles);

  // Filter vehicles
  const filteredVehicles = availableVehicles.filter(vehicle => {
    const matchesSearch = vehicle.variantName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.model?.modelName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.model?.brand?.brandName?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesBrand = selectedBrand === 'all' || vehicle.model?.brand?.brandId === selectedBrand;
    const matchesColor = selectedColor === 'all' || vehicle.colors.includes(selectedColor);
    const matchesPrice = vehicle.minPrice >= priceRange.min && vehicle.maxPrice <= priceRange.max;
    
    return matchesSearch && matchesBrand && matchesColor && matchesPrice;
  });
  
  console.log('Filtered vehicles:', filteredVehicles);

  // Sort vehicles
  const sortedVehicles = [...filteredVehicles].sort((a, b) => {
    switch (sortBy) {
      case 'price-low':
        return a.minPrice - b.minPrice;
      case 'price-high':
        return b.maxPrice - a.maxPrice;
      case 'name':
        return a.variantName.localeCompare(b.variantName);
      case 'brand':
        return a.model?.brand?.brandName.localeCompare(b.model?.brand?.brandName);
      default:
        return 0;
    }
  });

  const handleContactSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      
      // Validate required fields
      if (!contactForm.firstName.trim()) {
        toast.error('Vui lòng nhập tên');
        return;
      }
      if (!contactForm.lastName.trim()) {
        toast.error('Vui lòng nhập họ');
        return;
      }
      if (!contactForm.email.trim()) {
        toast.error('Vui lòng nhập email');
        return;
      }
      if (!contactForm.phone.trim()) {
        toast.error('Vui lòng nhập số điện thoại');
        return;
      }
      
      // Validate email format
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(contactForm.email)) {
        toast.error('Email không hợp lệ');
        return;
      }
      
      // Validate phone format (Vietnamese phone numbers)
      const phoneRegex = /^(\+84|84|0)[1-9][0-9]{8,9}$/;
      if (!phoneRegex.test(contactForm.phone.replace(/\s/g, ''))) {
        toast.error('Số điện thoại không hợp lệ');
        return;
      }
      
      // Validate postal code if provided
      if (contactForm.postalCode && !/^\d{5,6}$/.test(contactForm.postalCode)) {
        toast.error('Mã bưu điện phải có 5-6 chữ số');
        return;
      }
      
      // Create customer record
      const customerData = {
        firstName: contactForm.firstName.trim(),
        lastName: contactForm.lastName.trim(),
        email: contactForm.email.trim().toLowerCase(),
        phone: contactForm.phone.trim(),
        address: contactForm.address?.trim() || null,
        city: contactForm.city?.trim() || null,
        province: contactForm.province?.trim() || null,
        postalCode: contactForm.postalCode?.trim() || null,
        notes: `Quan tâm đến xe: ${contactForm.interestedVehicle}\n\nTin nhắn: ${contactForm.message || 'Không có tin nhắn'}\n\nTùy chọn bổ sung:\n${contactForm.wantTestDrive ? '- Muốn lái thử xe\n' : ''}${contactForm.wantFinancing ? '- Quan tâm đến tài chính/trả góp\n' : ''}${contactForm.wantInsurance ? '- Quan tâm đến bảo hiểm xe\n' : ''}${contactForm.wantAccessories ? '- Quan tâm đến phụ kiện\n' : ''}`
      };
      
      console.log('Creating customer with data:', customerData);
      
      try {
        const customerResponse = await publicCustomerAPI.createCustomer(customerData);
        const customer = customerResponse.data;
        
        console.log('Customer created successfully:', customer);
        
        // Continue with quotation creation...
        await handleQuotationCreation(customer, customerData);
        
      } catch (customerError) {
        console.error('Customer creation failed:', customerError);
        console.error('Customer error details:', customerError.response?.data);
        
        // Show detailed error information
        const errorMessage = customerError.response?.data?.message || customerError.message;
        console.error('Customer error message:', errorMessage);
        
        // Check if it's a validation error
        if (customerError.response?.status === 400) {
          toast.error(`Lỗi validation: ${errorMessage}. Vui lòng kiểm tra thông tin và thử lại.`);
        } else if (customerError.response?.status === 409) {
          toast.error('Email hoặc số điện thoại đã tồn tại. Vui lòng sử dụng thông tin khác.');
        } else if (customerError.response?.status === 404) {
          toast.error('API endpoint không tồn tại. Vui lòng liên hệ trực tiếp.');
        } else {
          toast.error('Không thể tạo khách hàng. Vui lòng thử lại sau.');
        }
        return;
      }
      
      setShowContactForm(false);
      setContactForm({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        city: '',
        province: '',
        postalCode: '',
        message: '',
        interestedVehicle: '',
        wantTestDrive: false,
        wantFinancing: false,
        wantInsurance: false,
        wantAccessories: false
      });
    } catch (error) {
      console.error('Error submitting contact form:', error);
      toast.error('Không thể gửi thông tin. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const openContactForm = (vehicle) => {
    setSelectedVehicle(vehicle);
    setContactForm(prev => ({
      ...prev,
      interestedVehicle: `${vehicle.model?.brand?.brandName} ${vehicle.model?.modelName} ${vehicle.variantName}`
    }));
    setShowContactForm(true);
  };

  const openVehicleDetail = (vehicle) => {
    setSelectedVehicle(vehicle);
    setShowVehicleDetail(true);
  };

  const handleQuotationCreation = async (customer, customerData) => {
    // If a specific vehicle is selected, create quotation
    if (selectedVehicle) {
      // Validate required fields
      if (!customer.customerId) {
        throw new Error('Customer ID is missing');
      }
      
      if (!selectedVehicle.variantId) {
        throw new Error('Vehicle variant ID is missing');
      }
      
      // Convert variantId to string if it's a number (backend might expect UUID string)
      let variantId = selectedVehicle.variantId;
      if (typeof variantId === 'number') {
        // If it's a number, try to find the actual UUID from the vehicle data
        // For now, we'll use the number as string, but this might need adjustment
        variantId = variantId.toString();
      }
      
      // Ensure variantId is not null or undefined
      if (!variantId) {
        throw new Error('Vehicle variant ID is missing or invalid');
      }
      
      const quotationData = {
        customerId: customer.customerId,
        variantId: variantId,
        colorId: null, // Will be selected later
        quotationDate: new Date().toISOString().split('T')[0],
        totalPrice: selectedVehicle.minPrice || 0,
        discountAmount: 0,
        finalPrice: selectedVehicle.minPrice || 0,
        validityDays: 7,
        status: 'pending',
        notes: `Báo giá tự động cho xe: ${contactForm.interestedVehicle}\n\nTin nhắn: ${contactForm.message || 'Không có tin nhắn'}`
      };
      
      // Validate quotation data
      if (!quotationData.customerId) {
        throw new Error('Customer ID is missing');
      }
      if (!quotationData.variantId) {
        throw new Error('Variant ID is missing');
      }
      if (quotationData.totalPrice <= 0) {
        throw new Error('Total price must be greater than 0');
      }
      
      console.log('Creating quotation with data:', quotationData);
      console.log('Selected vehicle:', selectedVehicle);
      
      try {
        await publicQuotationAPI.createQuotation(quotationData);
        toast.success('Cảm ơn bạn! Chúng tôi đã tạo báo giá và sẽ liên hệ lại sớm nhất.');
      } catch (quotationError) {
        console.error('Quotation creation failed:', quotationError);
        console.error('Quotation error details:', quotationError.response?.data);
        
        // Show detailed error information
        const errorMessage = quotationError.response?.data?.message || quotationError.message;
        console.error('Error message:', errorMessage);
        
        // Check if it's a validation error
        if (quotationError.response?.status === 400) {
          toast.error(`Lỗi validation: ${errorMessage}. Vui lòng kiểm tra thông tin và thử lại.`);
        } else if (quotationError.response?.status === 404) {
          toast.error('API endpoint không tồn tại. Vui lòng liên hệ trực tiếp.');
        } else {
          toast.error('Không thể tạo báo giá. Vui lòng liên hệ trực tiếp.');
        }
        
        // Fallback: Update customer notes with quotation request
        try {
          const updatedCustomerData = {
            ...customerData,
            notes: `${customerData.notes}\n\n[QUOTATION REQUEST] Xe: ${contactForm.interestedVehicle}, Giá: ${formatPrice(selectedVehicle.minPrice || 0)}`
          };
          await publicCustomerAPI.createCustomer(updatedCustomerData);
          console.log('Fallback: Updated customer with quotation request');
        } catch (fallbackError) {
          console.error('Fallback also failed:', fallbackError);
        }
      }
    } else {
      toast.success('Cảm ơn bạn đã quan tâm! Chúng tôi sẽ liên hệ lại sớm nhất.');
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND',
      minimumFractionDigits: 0
    }).format(price);
  };

  const getPriceRange = (vehicle) => {
    if (vehicle.minPrice === vehicle.maxPrice) {
      return formatPrice(vehicle.minPrice);
    }
    return `${formatPrice(vehicle.minPrice)} - ${formatPrice(vehicle.maxPrice)}`;
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="public-sales-page">
      {/* Header */}
      <header className="sales-header">
        <div className="container">
          <div className="header-content">
            <div className="logo-section">
              <Car size={40} className="logo-icon" />
              <div>
                <h1>EV Dealer Management</h1>
                <p>Khám phá thế giới xe điện</p>
              </div>
            </div>
            <div className="header-actions">
              <div className="contact-info">
                <div className="contact-item">
                  <Phone size={20} />
                  <span>Hotline: 1900-xxxx</span>
                </div>
                <div className="contact-item">
                  <Mail size={20} />
                  <span>sales@evdealer.com</span>
                </div>
              </div>
              <button 
                className="system-login-btn"
                onClick={() => navigate('/login')}
              >
                <LogIn size={20} />
                <span>Đăng nhập hệ thống</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="container">
          <div className="hero-content">
            <h2>Khám phá bộ sưu tập xe điện cao cấp</h2>
            <p>Trải nghiệm công nghệ tiên tiến với những mẫu xe điện hiện đại nhất</p>
            <div className="hero-stats">
              <div className="stat-item">
                <Car size={24} />
                <span>{availableVehicles.length} mẫu xe</span>
              </div>
              <div className="stat-item">
                <Battery size={24} />
                <span>100% điện</span>
              </div>
              <div className="stat-item">
                <Star size={24} />
                <span>Chất lượng cao</span>
              </div>
              {promotions.length > 0 && (
                <div className="stat-item">
                  <DollarSign size={24} />
                  <span>{promotions.length} khuyến mãi</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Promotions Section */}
      {promotions.length > 0 && (
        <section className="promotions-section">
          <div className="container">
            <div className="section-header">
              <h3>🎁 Khuyến mãi đặc biệt</h3>
            </div>
            <div className="promotions-grid">
              {promotions.slice(0, 3).map(promotion => (
                <div key={promotion.promotionId} className="promotion-card">
                  <div className="promotion-badge">
                    <DollarSign size={20} />
                    <span>Khuyến mãi</span>
                  </div>
                  <h4>{promotion.promotionName}</h4>
                  <p>{promotion.description}</p>
                  <div className="promotion-details">
                    <span className="discount">{promotion.discountPercentage}% giảm giá</span>
                    <span className="validity">Có hiệu lực đến: {new Date(promotion.endDate).toLocaleDateString('vi-VN')}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Search and Filter Section */}
      <section className="search-filter-section">
        <div className="container">
          <div className="search-bar">
            <div className="search-input">
              <Search size={20} />
              <input
                type="text"
                placeholder="Tìm kiếm xe theo tên, thương hiệu..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button 
              className="filter-toggle"
              onClick={() => setShowFilters(!showFilters)}
            >
              <Filter size={20} />
              Bộ lọc
              {showFilters ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            </button>
          </div>

          {showFilters && (
            <div className="filters-panel">
              <div className="filter-group">
                <label>Thương hiệu</label>
                <select 
                  value={selectedBrand} 
                  onChange={(e) => setSelectedBrand(e.target.value)}
                >
                  <option value="all">Tất cả thương hiệu</option>
                  {brands.map(brand => (
                    <option key={brand.brandId} value={brand.brandId}>
                      {brand.brandName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-group">
                <label>Màu sắc</label>
                <select 
                  value={selectedColor} 
                  onChange={(e) => setSelectedColor(e.target.value)}
                >
                  <option value="all">Tất cả màu sắc</option>
                  {colors.map(color => (
                    <option key={color.colorId} value={color.colorName}>
                      {color.colorName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-group">
                <label>Khoảng giá</label>
                <div className="price-range">
                  <input
                    type="number"
                    placeholder="Từ"
                    value={priceRange.min}
                    onChange={(e) => setPriceRange(prev => ({ ...prev, min: parseInt(e.target.value) || 0 }))}
                  />
                  <span>-</span>
                  <input
                    type="number"
                    placeholder="Đến"
                    value={priceRange.max}
                    onChange={(e) => setPriceRange(prev => ({ ...prev, max: parseInt(e.target.value) || 5000000000 }))}
                  />
                </div>
              </div>

              <div className="filter-group">
                <label>Sắp xếp</label>
                <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                  <option value="name">Tên A-Z</option>
                  <option value="brand">Thương hiệu</option>
                  <option value="price-low">Giá thấp đến cao</option>
                  <option value="price-high">Giá cao đến thấp</option>
                </select>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* Vehicles Grid */}
      <section className="vehicles-section">
        <div className="container">
          <div className="section-header">
            <h3>Xe có sẵn ({sortedVehicles.length})</h3>
          </div>
          
          {sortedVehicles.length === 0 ? (
            <div className="no-results">
              <Car size={48} />
              <h4>Không tìm thấy xe phù hợp</h4>
              <p>Hãy thử điều chỉnh bộ lọc để tìm xe mong muốn</p>
              <div className="debug-info" style={{marginTop: '20px', padding: '15px', background: '#f8f9fa', borderRadius: '8px', fontSize: '14px'}}>
                <p><strong>Debug Info:</strong></p>
                <p>Total vehicles loaded: {vehicles.length}</p>
                <p>Total inventory items: {inventory.length}</p>
                <p>Available vehicles: {availableVehicles.length}</p>
                <p>Filtered vehicles: {filteredVehicles.length}</p>
                <p>Search term: "{searchTerm}"</p>
                <p>Selected brand: {selectedBrand}</p>
                <p>Selected color: {selectedColor}</p>
                <button 
                  onClick={loadInitialData}
                  style={{
                    marginTop: '10px',
                    padding: '8px 16px',
                    background: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Reload Data
                </button>
              </div>
            </div>
          ) : (
            <div className="vehicles-grid">
              {sortedVehicles.map(vehicle => (
                <div key={vehicle.variantId} className="vehicle-card">
                  <div className="vehicle-image">
                    {vehicle.imageUrl ? (
                      <img 
                        src={vehicle.imageUrl} 
                        alt={vehicle.variantName}
                        className="vehicle-img"
                        onError={(e) => {
                          e.target.style.display = 'none';
                          e.target.nextSibling.style.display = 'flex';
                        }}
                      />
                    ) : null}
                    <div className="vehicle-placeholder" style={{display: vehicle.imageUrl ? 'none' : 'flex'}}>
                      <Car size={60} />
                    </div>
                    <div className="vehicle-badge">
                      <span>{vehicle.availableCount > 0 ? `${vehicle.availableCount} xe có sẵn` : 'Liên hệ để biết'}</span>
                    </div>
                    <div className="vehicle-overlay">
                      <button 
                        className="btn-overlay"
                        onClick={() => openVehicleDetail(vehicle)}
                      >
                        <Eye size={20} />
                        Xem chi tiết
                      </button>
                    </div>
                  </div>
                  
                  <div className="vehicle-info">
                    <div className="vehicle-header">
                      <h4 className="vehicle-title">{vehicle.variantName || 'Tên xe không xác định'}</h4>
                      <p className="brand-model">
                        {vehicle.model?.brand?.brandName || 'Thương hiệu'} {vehicle.model?.modelName || 'Mẫu xe'}
                      </p>
                    </div>
                    
                    <div className="vehicle-price">
                      <span className="price">{getPriceRange(vehicle)}</span>
                    </div>
                    
                    <div className="vehicle-specs">
                      <div className="spec-item">
                        <Battery size={16} />
                        <span>{vehicle.batteryCapacity || 'N/A'}kWh</span>
                      </div>
                      <div className="spec-item">
                        <Gauge size={16} />
                        <span>{vehicle.rangeKm || 'N/A'}km</span>
                      </div>
                      <div className="spec-item">
                        <Zap size={16} />
                        <span>{vehicle.powerKw || 'N/A'}kW</span>
                      </div>
                    </div>
                    
                    <div className="vehicle-colors">
                      <span>Màu sắc: </span>
                      <div className="color-list">
                        {vehicle.colors && vehicle.colors.length > 0 ? (
                          <>
                            {vehicle.colors.slice(0, 3).map((color, index) => (
                              <span key={index} className="color-tag">{color}</span>
                            ))}
                            {vehicle.colors.length > 3 && (
                              <span className="color-more">+{vehicle.colors.length - 3}</span>
                            )}
                          </>
                        ) : (
                          <span className="color-tag">Liên hệ để biết</span>
                        )}
                      </div>
                    </div>
                    
                    <div className="vehicle-actions">
                      <button 
                        className="btn btn-secondary"
                        onClick={() => openVehicleDetail(vehicle)}
                      >
                        <Eye size={16} />
                        Xem chi tiết
                      </button>
                      <button 
                        className="btn btn-primary"
                        onClick={() => openContactForm(vehicle)}
                      >
                        <Phone size={16} />
                        Đặt xe ngay
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Contact Form Modal */}
      {showContactForm && (
        <div className="modal-overlay">
          <div className="modal-container">
            <div className="modal-header">
              <h3>Liên hệ mua xe</h3>
              <button 
                className="modal-close"
                onClick={() => setShowContactForm(false)}
              >
                ×
              </button>
            </div>
            
            <form onSubmit={handleContactSubmit} className="contact-form">
              <div className="form-group">
                <label>Họ và tên *</label>
                <div className="name-inputs">
                  <input
                    type="text"
                    placeholder="Họ"
                    value={contactForm.firstName}
                    onChange={(e) => setContactForm(prev => ({ ...prev, firstName: e.target.value }))}
                    required
                  />
                  <input
                    type="text"
                    placeholder="Tên"
                    value={contactForm.lastName}
                    onChange={(e) => setContactForm(prev => ({ ...prev, lastName: e.target.value }))}
                    required
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  placeholder="email@example.com"
                  value={contactForm.email}
                  onChange={(e) => setContactForm(prev => ({ ...prev, email: e.target.value }))}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Số điện thoại *</label>
                <input
                  type="tel"
                  placeholder="0123-456-789"
                  value={contactForm.phone}
                  onChange={(e) => setContactForm(prev => ({ ...prev, phone: e.target.value }))}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Địa chỉ</label>
                <input
                  type="text"
                  placeholder="Số nhà, tên đường"
                  value={contactForm.address}
                  onChange={(e) => setContactForm(prev => ({ ...prev, address: e.target.value }))}
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Tỉnh/Thành phố</label>
                  <select
                    value={contactForm.province}
                    onChange={(e) => setContactForm(prev => ({ ...prev, province: e.target.value }))}
                  >
                    <option value="">Chọn tỉnh/thành phố</option>
                    <option value="Hồ Chí Minh">Hồ Chí Minh</option>
                    <option value="Hà Nội">Hà Nội</option>
                    <option value="Đà Nẵng">Đà Nẵng</option>
                    <option value="Cần Thơ">Cần Thơ</option>
                    <option value="An Giang">An Giang</option>
                    <option value="Bà Rịa - Vũng Tàu">Bà Rịa - Vũng Tàu</option>
                    <option value="Bắc Giang">Bắc Giang</option>
                    <option value="Bắc Kạn">Bắc Kạn</option>
                    <option value="Bạc Liêu">Bạc Liêu</option>
                    <option value="Bắc Ninh">Bắc Ninh</option>
                    <option value="Bến Tre">Bến Tre</option>
                    <option value="Bình Định">Bình Định</option>
                    <option value="Bình Dương">Bình Dương</option>
                    <option value="Bình Phước">Bình Phước</option>
                    <option value="Bình Thuận">Bình Thuận</option>
                    <option value="Cà Mau">Cà Mau</option>
                    <option value="Cao Bằng">Cao Bằng</option>
                    <option value="Đắk Lắk">Đắk Lắk</option>
                    <option value="Đắk Nông">Đắk Nông</option>
                    <option value="Điện Biên">Điện Biên</option>
                    <option value="Đồng Nai">Đồng Nai</option>
                    <option value="Đồng Tháp">Đồng Tháp</option>
                    <option value="Gia Lai">Gia Lai</option>
                    <option value="Hà Giang">Hà Giang</option>
                    <option value="Hà Nam">Hà Nam</option>
                    <option value="Hà Tĩnh">Hà Tĩnh</option>
                    <option value="Hải Dương">Hải Dương</option>
                    <option value="Hậu Giang">Hậu Giang</option>
                    <option value="Hòa Bình">Hòa Bình</option>
                    <option value="Hưng Yên">Hưng Yên</option>
                    <option value="Khánh Hòa">Khánh Hòa</option>
                    <option value="Kiên Giang">Kiên Giang</option>
                    <option value="Kon Tum">Kon Tum</option>
                    <option value="Lai Châu">Lai Châu</option>
                    <option value="Lâm Đồng">Lâm Đồng</option>
                    <option value="Lạng Sơn">Lạng Sơn</option>
                    <option value="Lào Cai">Lào Cai</option>
                    <option value="Long An">Long An</option>
                    <option value="Nam Định">Nam Định</option>
                    <option value="Nghệ An">Nghệ An</option>
                    <option value="Ninh Bình">Ninh Bình</option>
                    <option value="Ninh Thuận">Ninh Thuận</option>
                    <option value="Phú Thọ">Phú Thọ</option>
                    <option value="Phú Yên">Phú Yên</option>
                    <option value="Quảng Bình">Quảng Bình</option>
                    <option value="Quảng Nam">Quảng Nam</option>
                    <option value="Quảng Ngãi">Quảng Ngãi</option>
                    <option value="Quảng Ninh">Quảng Ninh</option>
                    <option value="Quảng Trị">Quảng Trị</option>
                    <option value="Sóc Trăng">Sóc Trăng</option>
                    <option value="Sơn La">Sơn La</option>
                    <option value="Tây Ninh">Tây Ninh</option>
                    <option value="Thái Bình">Thái Bình</option>
                    <option value="Thái Nguyên">Thái Nguyên</option>
                    <option value="Thanh Hóa">Thanh Hóa</option>
                    <option value="Thừa Thiên Huế">Thừa Thiên Huế</option>
                    <option value="Tiền Giang">Tiền Giang</option>
                    <option value="Trà Vinh">Trà Vinh</option>
                    <option value="Tuyên Quang">Tuyên Quang</option>
                    <option value="Vĩnh Long">Vĩnh Long</option>
                    <option value="Vĩnh Phúc">Vĩnh Phúc</option>
                    <option value="Yên Bái">Yên Bái</option>
                  </select>
                </div>
                
                <div className="form-group">
                  <label>Quận/Huyện</label>
                  <input
                    type="text"
                    placeholder="Quận/Huyện"
                    value={contactForm.city}
                    onChange={(e) => setContactForm(prev => ({ ...prev, city: e.target.value }))}
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Mã bưu điện</label>
                <input
                  type="text"
                  placeholder="100000"
                  value={contactForm.postalCode}
                  onChange={(e) => setContactForm(prev => ({ ...prev, postalCode: e.target.value }))}
                />
              </div>
              
              {/* Vehicle Selection Section */}
              <div className="vehicle-selection-section">
                <h4>Thông tin xe đã chọn</h4>
                
                {selectedVehicle && (
                  <div className="selected-vehicle-info">
                    <div className="vehicle-basic-info">
                      <h5>{selectedVehicle.model?.brand?.brandName} {selectedVehicle.model?.modelName} {selectedVehicle.variantName}</h5>
                      <div className="vehicle-price">
                        <span className="price-label">Giá từ:</span>
                        <span className="price-value">{formatPrice(selectedVehicle.minPrice || 0)}</span>
                      </div>
                    </div>
                    
                    {/* Vehicle Specifications */}
                    <div className="vehicle-specs">
                      <h6>Thông số kỹ thuật:</h6>
                      <div className="specs-grid">
                        {selectedVehicle.batteryCapacity && (
                          <div className="spec-item">
                            <span className="spec-label">Dung lượng pin:</span>
                            <span className="spec-value">{selectedVehicle.batteryCapacity} kWh</span>
                          </div>
                        )}
                        {selectedVehicle.rangeKm && (
                          <div className="spec-item">
                            <span className="spec-label">Tầm hoạt động:</span>
                            <span className="spec-value">{selectedVehicle.rangeKm} km</span>
                          </div>
                        )}
                        {selectedVehicle.powerKw && (
                          <div className="spec-item">
                            <span className="spec-label">Công suất:</span>
                            <span className="spec-value">{selectedVehicle.powerKw} kW</span>
                          </div>
                        )}
                        {selectedVehicle.maxSpeed && (
                          <div className="spec-item">
                            <span className="spec-label">Tốc độ tối đa:</span>
                            <span className="spec-value">{selectedVehicle.maxSpeed} km/h</span>
                          </div>
                        )}
                        {selectedVehicle.chargingTime && (
                          <div className="spec-item">
                            <span className="spec-label">Thời gian sạc:</span>
                            <span className="spec-value">{selectedVehicle.chargingTime}</span>
                          </div>
                        )}
                        {selectedVehicle.seatingCapacity && (
                          <div className="spec-item">
                            <span className="spec-label">Số chỗ ngồi:</span>
                            <span className="spec-value">{selectedVehicle.seatingCapacity} chỗ</span>
                          </div>
                        )}
                      </div>
                    </div>
                    
                    {/* Available Colors */}
                    {selectedVehicle.colors && selectedVehicle.colors.length > 0 && (
                      <div className="vehicle-colors-selection">
                        <h6>Màu sắc có sẵn:</h6>
                        <div className="colors-list">
                          {selectedVehicle.colors.map((color, index) => (
                            <span key={index} className="color-option">
                              {color}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    {/* Inventory Status */}
                    <div className="inventory-status">
                      <h6>Tình trạng kho:</h6>
                      <div className="status-info">
                        {selectedVehicle.inventoryCount > 0 ? (
                          <span className="status-available">
                            ✅ Có sẵn ({selectedVehicle.inventoryCount} xe)
                          </span>
                        ) : (
                          <span className="status-unavailable">
                            ⏳ Đặt hàng trước
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                )}
                
                {/* Vehicle Selection Options */}
                <div className="vehicle-options">
                  <h6>Tùy chọn bổ sung:</h6>
                  <div className="options-list">
                    <label className="option-item">
                      <input
                        type="checkbox"
                        checked={contactForm.wantTestDrive || false}
                        onChange={(e) => setContactForm(prev => ({ ...prev, wantTestDrive: e.target.checked }))}
                      />
                      <span>Muốn lái thử xe</span>
                    </label>
                    <label className="option-item">
                      <input
                        type="checkbox"
                        checked={contactForm.wantFinancing || false}
                        onChange={(e) => setContactForm(prev => ({ ...prev, wantFinancing: e.target.checked }))}
                      />
                      <span>Quan tâm đến tài chính/trả góp</span>
                    </label>
                    <label className="option-item">
                      <input
                        type="checkbox"
                        checked={contactForm.wantInsurance || false}
                        onChange={(e) => setContactForm(prev => ({ ...prev, wantInsurance: e.target.checked }))}
                      />
                      <span>Quan tâm đến bảo hiểm xe</span>
                    </label>
                    <label className="option-item">
                      <input
                        type="checkbox"
                        checked={contactForm.wantAccessories || false}
                        onChange={(e) => setContactForm(prev => ({ ...prev, wantAccessories: e.target.checked }))}
                      />
                      <span>Quan tâm đến phụ kiện</span>
                    </label>
                  </div>
                </div>
              </div>
              
              <div className="form-group">
                <label>Tin nhắn</label>
                <textarea
                  placeholder="Bạn có câu hỏi gì về xe này không?"
                  value={contactForm.message}
                  onChange={(e) => setContactForm(prev => ({ ...prev, message: e.target.value }))}
                  rows={4}
                />
              </div>
              
              <div className="form-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary"
                  onClick={() => setShowContactForm(false)}
                >
                  Hủy
                </button>
                <button 
                  type="submit" 
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? <LoadingSpinner /> : 'Gửi thông tin'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Vehicle Detail Modal */}
      {showVehicleDetail && selectedVehicle && (
        <div className="modal-overlay">
          <div className="modal-content vehicle-detail-modal">
            <div className="modal-header">
              <h3>Chi tiết xe</h3>
              <button 
                className="close-btn"
                onClick={() => setShowVehicleDetail(false)}
              >
                ×
              </button>
            </div>
            
            <div className="modal-body">
              <div className="vehicle-detail-content">
                <div className="vehicle-detail-image">
                  {selectedVehicle.imageUrl ? (
                    <img 
                      src={selectedVehicle.imageUrl} 
                      alt={selectedVehicle.variantName}
                      className="detail-img"
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.nextSibling.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div className="detail-placeholder" style={{display: selectedVehicle.imageUrl ? 'none' : 'flex'}}>
                    <Car size={120} />
                  </div>
                </div>
                
                <div className="vehicle-detail-info">
                  <div className="detail-header">
                    <h2>{selectedVehicle.variantName || 'Tên xe không xác định'}</h2>
                    <p className="detail-brand">
                      {selectedVehicle.model?.brand?.brandName || 'Thương hiệu'} {selectedVehicle.model?.modelName || 'Mẫu xe'}
                    </p>
                    <div className="detail-price">
                      <span className="price-large">{getPriceRange(selectedVehicle)}</span>
                    </div>
                  </div>
                  
                  <div className="detail-specs">
                    <h4>Thông số kỹ thuật</h4>
                    <div className="specs-grid">
                      <div className="spec-item">
                        <Battery size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Dung lượng pin</span>
                          <span className="spec-value">{selectedVehicle.batteryCapacity || 'N/A'} kWh</span>
                        </div>
                      </div>
                      <div className="spec-item">
                        <Gauge size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Tầm hoạt động</span>
                          <span className="spec-value">{selectedVehicle.rangeKm || 'N/A'} km</span>
                        </div>
                      </div>
                      <div className="spec-item">
                        <Zap size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Công suất</span>
                          <span className="spec-value">{selectedVehicle.powerKw || 'N/A'} kW</span>
                        </div>
                      </div>
                      <div className="spec-item">
                        <Gauge size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Tốc độ tối đa</span>
                          <span className="spec-value">{selectedVehicle.maxSpeed || 'N/A'} km/h</span>
                        </div>
                      </div>
                      <div className="spec-item">
                        <Clock size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Thời gian sạc</span>
                          <span className="spec-value">{selectedVehicle.chargingTime || 'N/A'} phút</span>
                        </div>
                      </div>
                      <div className="spec-item">
                        <Users size={20} />
                        <div className="spec-content">
                          <span className="spec-label">Số chỗ ngồi</span>
                          <span className="spec-value">{selectedVehicle.seatingCapacity || 'N/A'} chỗ</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="detail-colors">
                    <h4>Màu sắc có sẵn</h4>
                    <div className="colors-grid">
                      {selectedVehicle.colors && selectedVehicle.colors.length > 0 ? (
                        selectedVehicle.colors.map((color, index) => (
                          <div key={index} className="color-option">
                            <span className="color-name">{color}</span>
                          </div>
                        ))
                      ) : (
                        <p>Liên hệ để biết màu sắc có sẵn</p>
                      )}
                    </div>
                  </div>
                  
                  <div className="detail-availability">
                    <h4>Tình trạng kho</h4>
                    <div className="availability-info">
                      <span className="availability-status">
                        {selectedVehicle.availableCount > 0 ? 
                          `Có ${selectedVehicle.availableCount} xe sẵn sàng giao hàng` : 
                          'Liên hệ để biết tình trạng kho'
                        }
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="modal-footer">
              <button 
                className="btn btn-secondary"
                onClick={() => setShowVehicleDetail(false)}
              >
                Đóng
              </button>
              <button 
                className="btn btn-primary"
                onClick={() => {
                  setShowVehicleDetail(false);
                  openContactForm(selectedVehicle);
                }}
              >
                <Phone size={16} />
                Đặt xe ngay
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="sales-footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h4>Liên hệ</h4>
              <div className="contact-info">
                <div className="contact-item">
                  <MapPin size={16} />
                  <span>123 Đường ABC, Quận 1, TP.HCM</span>
                </div>
                <div className="contact-item">
                  <Phone size={16} />
                  <span>1900-xxxx</span>
                </div>
                <div className="contact-item">
                  <Mail size={16} />
                  <span>sales@evdealer.com</span>
                </div>
              </div>
            </div>
            
            <div className="footer-section">
              <h4>Giờ làm việc</h4>
              <div className="working-hours">
                <div className="hour-item">
                  <Calendar size={16} />
                  <span>Thứ 2 - Thứ 6: 8:00 - 18:00</span>
                </div>
                <div className="hour-item">
                  <Calendar size={16} />
                  <span>Thứ 7: 8:00 - 12:00</span>
                </div>
                <div className="hour-item">
                  <Calendar size={16} />
                  <span>Chủ nhật: Nghỉ</span>
                </div>
              </div>
            </div>
            
            <div className="footer-section">
              <h4>Dịch vụ</h4>
              <ul className="service-list">
                <li>Bán xe điện</li>
                <li>Bảo hành</li>
                <li>Bảo dưỡng</li>
                <li>Phụ tùng</li>
                <li>Tư vấn</li>
              </ul>
            </div>
          </div>
          
          <div className="footer-bottom">
            <p>&copy; 2024 EV Dealer Management. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default PublicSalesPage;
