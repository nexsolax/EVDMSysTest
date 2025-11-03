import React, { useState, useEffect } from 'react';
import { Search, Filter, Phone, Mail, Car, Zap, Shield, Eye, Calendar, Quote, ArrowLeft } from 'lucide-react';
import { publicInventoryAPI, publicVehicleAPI } from '../services/api';
import QuoteModal from '../components/modals/QuoteModal';
import AppointmentModal from '../components/modals/AppointmentModal';
import FeedbackModal from '../components/modals/FeedbackModal';
import VehicleImage from '../components/VehicleImage';
import './PublicSalesPage.css';

const PublicSalesPage = () => {
  const [vehicles, setVehicles] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [showAppointmentModal, setShowAppointmentModal] = useState(false);
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'list'
  const [selectedVehicleDetail, setSelectedVehicleDetail] = useState(null);
  const [selectedVehicles, setSelectedVehicles] = useState([]);
  const [showComparison, setShowComparison] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [vehiclesRes, brandsRes] = await Promise.all([
        publicInventoryAPI.getInventory(),
        publicVehicleAPI.getBrands()
      ]);
      
      setVehicles(vehiclesRes.data || []);
      setBrands(brandsRes.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
      // Fallback data khi API không hoạt động
      setVehicles([
        {
          inventoryId: '1',
          variant: {
            variantName: 'Tesla Model 3 Standard Range',
            priceBase: 1200000000,
            batteryCapacity: 60,
            rangeKm: 400,
            powerKw: 200,
            variantImageUrl: null
          },
          model: { 
            modelName: 'Model 3',
            brand: { brandName: 'Tesla' }
          },
          sellingPrice: 1200000000,
          status: 'available'
        },
        {
          inventoryId: '2',
          variant: {
            variantName: 'BYD Atto 3 Standard',
            priceBase: 800000000,
            batteryCapacity: 50.1,
            rangeKm: 480,
            powerKw: 150,
            variantImageUrl: null
          },
          model: { 
            modelName: 'Atto 3',
            brand: { brandName: 'BYD' }
          },
          sellingPrice: 800000000,
          status: 'available'
        },
        {
          inventoryId: '3',
          variant: {
            variantName: 'BMW iX xDrive50',
            priceBase: 2500000000,
            batteryCapacity: 111.5,
            rangeKm: 630,
            powerKw: 385,
            variantImageUrl: null
          },
          model: { 
            modelName: 'iX',
            brand: { brandName: 'BMW' }
          },
          sellingPrice: 2500000000,
          status: 'available'
        }
      ]);
      setBrands([
        { brandId: '1', brandName: 'Tesla' },
        { brandId: '2', brandName: 'BMW' },
        { brandId: '3', brandName: 'Audi' },
      ]);
      // setPromotions([
      //   { promotionId: '1', promotionName: 'Giảm giá 10%', description: 'Áp dụng cho Model 3', startDate: '2024-01-01', endDate: '2024-12-31' },
      // ]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleFilterByBrand = (brand) => {
    setSelectedBrand(brand);
  };

  const handleRequestQuote = (vehicle) => {
    setSelectedVehicle(vehicle);
    setShowQuoteModal(true);
  };

  const handleBookAppointment = (vehicle) => {
    setSelectedVehicle(vehicle);
    setShowAppointmentModal(true);
  };


  const handleViewDetail = (vehicle) => {
    setSelectedVehicleDetail(vehicle);
    setViewMode('detail');
  };

  const handleBackToGrid = () => {
    setViewMode('grid');
    setSelectedVehicleDetail(null);
  };

  const handleViewModeChange = (mode) => {
    setViewMode(mode);
  };

  const handleRefresh = () => {
    loadData();
  };

  const handleLogin = () => {
    // Redirect to login page
    window.location.href = '/login';
  };

  const handleAddToComparison = (vehicle) => {
    if (selectedVehicles.length >= 3) {
      alert('Chỉ có thể so sánh tối đa 3 xe');
      return;
    }
    if (!selectedVehicles.find(v => v.inventoryId === vehicle.inventoryId)) {
      setSelectedVehicles([...selectedVehicles, vehicle]);
    }
  };

  const handleRemoveFromComparison = (vehicleId) => {
    setSelectedVehicles(selectedVehicles.filter(v => v.inventoryId !== vehicleId));
  };

  const handleCompare = () => {
    if (selectedVehicles.length < 2) {
      alert('Vui lòng chọn ít nhất 2 xe để so sánh');
      return;
    }
    setShowComparison(true);
  };

  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesSearch = (vehicle.variant?.variantName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
                         (vehicle.variant?.model?.modelName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
                         (vehicle.variant?.model?.brand?.brandName?.toLowerCase() || '').includes(searchTerm.toLowerCase());
    
    const matchesBrand = !selectedBrand || vehicle.variant?.model?.brand?.brandName === selectedBrand;
    
    return matchesSearch && matchesBrand;
  });

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner">⏳</div>
        <p>Đang tải dữ liệu...</p>
      </div>
    );
  }

  if (viewMode === 'detail' && selectedVehicleDetail) {
    return (
      <div className="vehicle-detail-page">
        <div className="detail-header">
          <button className="back-btn" onClick={handleBackToGrid}>
            <ArrowLeft className="back-icon" />
            Quay lại danh sách
          </button>
        </div>
        
        <div className="vehicle-detail-content">
          <div className="vehicle-detail-image">
            <VehicleImage 
              vehicle={selectedVehicleDetail} 
              className="detail-main-image"
              size={48}
            />
          </div>
          
          <div className="vehicle-detail-info">
            <h1 className="detail-title">{selectedVehicleDetail.variant?.variantName}</h1>
            <p className="detail-brand">{selectedVehicleDetail.variant?.model?.brand?.brandName} • {selectedVehicleDetail.variant?.model?.modelName}</p>
            
            <div className="detail-price">
              <span className="price-amount">{selectedVehicleDetail.sellingPrice?.toLocaleString('vi-VN')} VNĐ</span>
              <span className="price-label">Giá niêm yết</span>
            </div>
            
            <div className="detail-specs">
              <div className="spec-item">
                <Zap className="spec-icon" />
                <div className="spec-content">
                  <span className="spec-label">Công suất</span>
                  <span className="spec-value">{selectedVehicleDetail.variant?.powerKw || 'N/A'} kW</span>
                </div>
              </div>
              <div className="spec-item">
                <Car className="spec-icon" />
                <div className="spec-content">
                  <span className="spec-label">Tầm hoạt động</span>
                  <span className="spec-value">{selectedVehicleDetail.variant?.rangeKm || 'N/A'} km</span>
                </div>
              </div>
              <div className="spec-item">
                <Shield className="spec-icon" />
                <div className="spec-content">
                  <span className="spec-label">Dung lượng pin</span>
                  <span className="spec-value">{selectedVehicleDetail.variant?.batteryCapacity || 'N/A'} kWh</span>
                </div>
              </div>
            </div>
            
            <div className="detail-actions">
              <button 
                className="btn-lg btn-primary"
                onClick={() => handleRequestQuote(selectedVehicleDetail)}
              >
                <Quote className="btn-icon" />
                Yêu cầu báo giá
              </button>
              <button 
                className="btn-lg btn-secondary"
                onClick={() => handleBookAppointment(selectedVehicleDetail)}
              >
                <Calendar className="btn-icon" />
                Đặt lịch lái thử
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="public-sales-page">
      {/* Header */}
      <header className="public-header">
        <div className="container">
          <div className="header-content">
            <div className="logo">
              <Car className="logo-icon" />
              <span className="logo-text">EVDM</span>
            </div>
            <div className="header-actions">
              <div className="contact-info">
                <div className="contact-item">
                  <Phone className="contact-icon" />
                  <span>0123 456 789</span>
                </div>
                <div className="contact-item">
                  <Mail className="contact-icon" />
                  <span>info@evdm.com</span>
                </div>
              </div>
              <button className="login-btn" onClick={handleLogin}>Đăng nhập</button>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-background"></div>
        <div className="container">
          <div className="hero-content">
            <h1 className="hero-title">Xe điện thông minh</h1>
            <p className="hero-subtitle">Khám phá tương lai di chuyển bền vững với dòng xe điện hiện đại</p>
            <div className="hero-stats">
              <div className="stat-item">
                <Car className="stat-icon" />
                <span>50+ Mẫu xe</span>
              </div>
              <div className="stat-item">
                <Zap className="stat-icon" />
                <span>100% Điện</span>
              </div>
              <div className="stat-item">
                <Shield className="stat-icon" />
                <span>Bảo hành 8 năm</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Search and Filter */}
      <section className="search-section">
          <div className="container">
          <div className="search-controls">
          <div className="search-bar">
              <Search className="search-icon" />
              <input
                type="text"
                placeholder="Tìm kiếm xe theo tên, thương hiệu..."
                value={searchTerm}
                onChange={handleSearch}
                className="search-input"
              />
            </div>
            <button 
              className="refresh-btn"
              onClick={handleRefresh}
              title="Làm mới dữ liệu"
            >
              🔄
            </button>
            <div className="filter-controls">
              <Filter className="filter-icon" />
                <select 
                  value={selectedBrand} 
                onChange={(e) => handleFilterByBrand(e.target.value)}
                className="filter-select"
                >
                <option value="">Tất cả thương hiệu</option>
                  {brands.map(brand => (
                  <option key={brand.brandId} value={brand.brandName}>
                      {brand.brandName}
                    </option>
                  ))}
                </select>
              </div>
              </div>
        </div>
      </section>

      {/* Vehicles Section */}
      <section className="vehicles-section">
        <div className="container">
          <div className="section-header">
            <h2 className="section-title">Xe có sẵn ({filteredVehicles.length})</h2>
            <div className="view-controls">
                <button 
                className={`view-btn ${viewMode === 'grid' ? 'active' : ''}`}
                onClick={() => handleViewModeChange('grid')}
              >
                Lưới
              </button>
              <button 
                className={`view-btn ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => handleViewModeChange('list')}
              >
                Danh sách
                </button>
              </div>
            </div>
              
          {viewMode === 'grid' ? (
            <div className="vehicles-grid">
              {filteredVehicles.map(vehicle => (
                <div key={vehicle.inventoryId} className="vehicle-card">
                  <div className="vehicle-image">
                    <VehicleImage 
                      vehicle={vehicle} 
                      className="vehicle-thumbnail"
                      size={24}
                    />
                  </div>
                  
                  <div className="vehicle-info">
                    <h3 className="vehicle-name">{vehicle.variant?.variantName || 'N/A'}</h3>
                    <div className="vehicle-details">
                      <div className="detail-item">
                        <Car className="detail-icon" />
                        <span>{vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName}</span>
                    </div>
                      <div className="detail-item">
                        <Zap className="detail-icon" />
                        <span>{vehicle.variant?.batteryCapacity || 'N/A'} kWh</span>
                    </div>
                      <div className="detail-item">
                        <Shield className="detail-icon" />
                        <span>{vehicle.variant?.rangeKm || 'N/A'} km</span>
                      </div>
                    </div>
                    
                    <div className="vehicle-price">
                      <span className="price">{vehicle.sellingPrice?.toLocaleString('vi-VN')} VNĐ</span>
                      <span className="price-unit">/xe</span>
                    </div>
                    
                    <div className="vehicle-actions">
                      <button 
                        className="action-btn primary"
                        onClick={() => handleViewDetail(vehicle)}
                      >
                        <Eye className="btn-icon" />
                        Xem chi tiết
                      </button>
                      <button 
                        className="action-btn secondary"
                        onClick={() => handleAddToComparison(vehicle)}
                        disabled={selectedVehicles.find(v => v.inventoryId === vehicle.inventoryId)}
                      >
                        <Zap className="btn-icon" />
                        {selectedVehicles.find(v => v.inventoryId === vehicle.inventoryId) ? 'Đã chọn' : 'So sánh'}
                      </button>
                      <button 
                        className="action-btn secondary"
                        onClick={() => handleRequestQuote(vehicle)}
                      >
                        <Quote className="btn-icon" />
                        Báo giá
                      </button>
            </div>
                </div>
              </div>
              ))}
              </div>
          ) : (
            <div className="vehicles-list">
              {filteredVehicles.map(vehicle => (
                <div key={vehicle.inventoryId} className="vehicle-list-item">
                  <div className="list-image">
                    <VehicleImage 
                      vehicle={vehicle} 
                      className="vehicle-thumbnail"
                      size={24}
                />
              </div>
                  <div className="list-content">
                    <h3 className="list-title">{vehicle.variant?.variantName || 'N/A'}</h3>
                    <p className="list-brand">{vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName}</p>
                    <div className="list-specs">
                      <span className="spec-item">
                        <Zap className="spec-icon" />
                        {vehicle.variant?.batteryCapacity || 'N/A'} kWh
                            </span>
                      <span className="spec-item">
                        <Shield className="spec-icon" />
                        {vehicle.variant?.rangeKm || 'N/A'} km
                          </span>
                      <span className="spec-item">
                        <Car className="spec-icon" />
                        {vehicle.variant?.powerKw || 'N/A'} kW
                          </span>
                      </div>
                    </div>
                  <div className="list-price">
                    <span className="price">{vehicle.sellingPrice?.toLocaleString('vi-VN')} VNĐ</span>
                  </div>
                  <div className="list-actions">
                    <button 
                      className="action-btn primary"
                      onClick={() => handleViewDetail(vehicle)}
                    >
                      <Eye className="btn-icon" />
                      Xem chi tiết
                    </button>
                    <button 
                      className="action-btn secondary"
                      onClick={() => handleAddToComparison(vehicle)}
                      disabled={selectedVehicles.find(v => v.inventoryId === vehicle.inventoryId)}
                    >
                      <Zap className="btn-icon" />
                      {selectedVehicles.find(v => v.inventoryId === vehicle.inventoryId) ? 'Đã chọn' : 'So sánh'}
                    </button>
                    <button 
                      className="action-btn secondary"
                      onClick={() => handleRequestQuote(vehicle)}
                    >
                      <Quote className="btn-icon" />
                      Báo giá
                    </button>
              </div>
          </div>
              ))}
        </div>
      )}
            </div>
      </section>

      {/* Comparison Bar */}
      {selectedVehicles.length > 0 && (
        <div className="comparison-bar">
          <div className="container">
            <div className="comparison-content">
              <div className="comparison-info">
                <h3>Đã chọn {selectedVehicles.length} xe để so sánh</h3>
                <div className="selected-vehicles">
                  {selectedVehicles.map(vehicle => (
                    <div key={vehicle.inventoryId} className="selected-vehicle">
                      <span>{vehicle.variant?.variantName}</span>
                      <button 
                        className="remove-btn"
                        onClick={() => handleRemoveFromComparison(vehicle.inventoryId)}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
              <div className="comparison-actions">
                <button 
                  className="btn btn-secondary"
                  onClick={() => setSelectedVehicles([])}
                >
                  Xóa tất cả
                </button>
                <button 
                  className="btn btn-primary"
                  onClick={handleCompare}
                  disabled={selectedVehicles.length < 2}
                >
                  <Zap className="btn-icon" />
                  So sánh xe
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Comparison Modal */}
      {showComparison && (
        <div className="comparison-modal">
          <div className="comparison-modal-content">
            <div className="comparison-modal-header">
              <h2>So sánh xe</h2>
              <button 
                className="close-btn"
                onClick={() => setShowComparison(false)}
              >
                ×
              </button>
            </div>
            <div className="comparison-table">
              <table>
                <thead>
                  <tr>
                    <th>Thông số</th>
                    {selectedVehicles.map(vehicle => (
                      <th key={vehicle.inventoryId}>
                        {vehicle.variant?.variantName}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Thương hiệu</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.variant?.model?.brand?.brandName}
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Dòng xe</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.variant?.model?.modelName}
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Giá bán</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.sellingPrice?.toLocaleString('vi-VN')} VNĐ
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Dung lượng pin</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.variant?.batteryCapacity} kWh
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Tầm hoạt động</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.variant?.rangeKm} km
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Công suất</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.inventoryId}>
                        {vehicle.variant?.powerKw} kW
                      </td>
                    ))}
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="public-footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h3>Liên hệ</h3>
              <p>Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM</p>
              <p>Điện thoại: 0123 456 789</p>
              <p>Email: info@evdm.com</p>
                </div>
            <div className="footer-section">
              <h3>Dịch vụ</h3>
              <ul>
                <li>Bán xe điện</li>
                <li>Bảo hành</li>
                <li>Sửa chữa</li>
                <li>Phụ tùng</li>
              </ul>
            </div>
            <div className="footer-section">
              <h3>Hỗ trợ</h3>
              <ul>
                <li>Hướng dẫn mua hàng</li>
                <li>Chính sách bảo hành</li>
                <li>FAQ</li>
                <li>Liên hệ</li>
              </ul>
          </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 EVDM. Tất cả quyền được bảo lưu.</p>
          </div>
        </div>
      </footer>

      {/* Modals */}
      <QuoteModal
        isOpen={showQuoteModal}
        onClose={() => setShowQuoteModal(false)}
        vehicle={selectedVehicle}
      />
      
      <AppointmentModal
        isOpen={showAppointmentModal}
        onClose={() => setShowAppointmentModal(false)}
        vehicle={selectedVehicle}
      />
      
      <FeedbackModal
        isOpen={showFeedbackModal}
        onClose={() => setShowFeedbackModal(false)}
        vehicle={selectedVehicle}
      />
    </div>
  );
};

export default PublicSalesPage;