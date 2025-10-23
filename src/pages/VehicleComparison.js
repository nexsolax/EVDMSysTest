import React, { useState, useEffect } from 'react';
import { useQuery } from 'react-query';
import { toast } from 'react-hot-toast';
import { 
  Car,
  Battery,
  Gauge,
  Zap,
  Clock,
  Users,
  Scale,
  Ruler,
  DollarSign,
  Star,
  Check,
  X,
  Plus,
  Trash2,
  Search,
  Filter
} from 'lucide-react';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { vehicleAPI } from '../services/api';
import './VehicleComparison.css';

const VehicleComparison = () => {
  const [selectedVehicles, setSelectedVehicles] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [brandFilter, setBrandFilter] = useState('all');
  const [priceRangeFilter, setPriceRangeFilter] = useState('all');
  const [maxVehicles] = useState(4); // Maximum 4 vehicles for comparison

  // Fetch vehicles
  const { data: vehicles, isLoading, error } = useQuery(
    'vehicles',
    () => vehicleAPI.getVariants(),
    {
      select: (response) => response.data || []
    }
  );

  // Filter vehicles
  const filteredVehicles = vehicles?.filter(vehicle => {
    const matchesSearch = vehicle.brand?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.model?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.variant?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesBrand = brandFilter === 'all' || vehicle.brand === brandFilter;
    
    let matchesPrice = true;
    if (priceRangeFilter !== 'all') {
      const price = vehicle.price || 0;
      switch (priceRangeFilter) {
        case 'under_500m':
          matchesPrice = price < 500000000;
          break;
        case '500m_1b':
          matchesPrice = price >= 500000000 && price < 1000000000;
          break;
        case '1b_2b':
          matchesPrice = price >= 1000000000 && price < 2000000000;
          break;
        case 'over_2b':
          matchesPrice = price >= 2000000000;
          break;
      }
    }
    
    return matchesSearch && matchesBrand && matchesPrice;
  }) || [];

  // Get unique brands for filter
  const brands = [...new Set(vehicles?.map(v => v.brand))].filter(Boolean);

  // Handle vehicle selection
  const handleAddVehicle = (vehicle) => {
    if (selectedVehicles.length >= maxVehicles) {
      toast.error(`Chỉ có thể so sánh tối đa ${maxVehicles} xe`);
      return;
    }
    
    if (selectedVehicles.find(v => v.vehicleId === vehicle.vehicleId)) {
      toast.error('Xe này đã được chọn để so sánh');
      return;
    }
    
    setSelectedVehicles([...selectedVehicles, vehicle]);
    toast.success(`Đã thêm ${vehicle.brand} ${vehicle.model} vào danh sách so sánh`);
  };

  const handleRemoveVehicle = (vehicleId) => {
    setSelectedVehicles(selectedVehicles.filter(v => v.vehicleId !== vehicleId));
  };

  const handleClearAll = () => {
    setSelectedVehicles([]);
  };

  if (isLoading) return <LoadingSpinner />;
  if (error) return <div className="error">Không thể tải dữ liệu xe</div>;

  return (
    <div className="vehicle-comparison">
      <div className="page-header">
        <div className="header-content">
          <h1>
            <Car className="icon" />
            So sánh mẫu xe
          </h1>
          <p>So sánh tính năng và thông số kỹ thuật giữa các mẫu xe điện</p>
        </div>
        {selectedVehicles.length > 0 && (
          <button
            className="btn-clear"
            onClick={handleClearAll}
          >
            <Trash2 size={20} />
            Xóa tất cả
          </button>
        )}
      </div>

      <div className="filters">
        <div className="filter-group">
          <div className="search-box">
            <Search size={20} />
            <input
              type="text"
              placeholder="Tìm kiếm theo thương hiệu, dòng xe..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          <select
            value={brandFilter}
            onChange={(e) => setBrandFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tất cả thương hiệu</option>
            {brands.map(brand => (
              <option key={brand} value={brand}>{brand}</option>
            ))}
          </select>

          <select
            value={priceRangeFilter}
            onChange={(e) => setPriceRangeFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tất cả mức giá</option>
            <option value="under_500m">Dưới 500 triệu</option>
            <option value="500m_1b">500 triệu - 1 tỷ</option>
            <option value="1b_2b">1 tỷ - 2 tỷ</option>
            <option value="over_2b">Trên 2 tỷ</option>
          </select>
        </div>
      </div>

      <div className="comparison-container">
        {/* Vehicle Selection */}
        <div className="vehicle-selection">
          <h2>Chọn xe để so sánh</h2>
          <div className="vehicle-grid">
            {filteredVehicles.map(vehicle => (
              <div key={vehicle.vehicleId} className="vehicle-card">
                <div className="vehicle-image">
                  <Car size={48} />
                </div>
                <div className="vehicle-info">
                  <h3>{vehicle.brand} {vehicle.model}</h3>
                  <p className="variant">{vehicle.variant}</p>
                  <p className="price">
                    <DollarSign size={16} />
                    {vehicle.price?.toLocaleString()} VNĐ
                  </p>
                  <div className="specs">
                    <div className="spec">
                      <Battery size={14} />
                      {vehicle.batteryCapacity || 'N/A'} kWh
                    </div>
                    <div className="spec">
                      <Gauge size={14} />
                      {vehicle.range || 'N/A'} km
                    </div>
                  </div>
                </div>
                <button
                  className="btn-add"
                  onClick={() => handleAddVehicle(vehicle)}
                  disabled={selectedVehicles.find(v => v.vehicleId === vehicle.vehicleId) || selectedVehicles.length >= maxVehicles}
                >
                  <Plus size={16} />
                  {selectedVehicles.find(v => v.vehicleId === vehicle.vehicleId) ? 'Đã chọn' : 'Thêm'}
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Comparison Table */}
        {selectedVehicles.length > 0 && (
          <div className="comparison-table">
            <h2>Bảng so sánh</h2>
            <div className="table-container">
              <table className="comparison-table-content">
                <thead>
                  <tr>
                    <th>Thông số</th>
                    {selectedVehicles.map(vehicle => (
                      <th key={vehicle.vehicleId} className="vehicle-header">
                        <div className="vehicle-title">
                          <h3>{vehicle.brand} {vehicle.model}</h3>
                          <p>{vehicle.variant}</p>
                        </div>
                        <button
                          className="btn-remove"
                          onClick={() => handleRemoveVehicle(vehicle.vehicleId)}
                        >
                          <X size={16} />
                        </button>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {/* Basic Information */}
                  <tr className="section-header">
                    <td colSpan={selectedVehicles.length + 1}>Thông tin cơ bản</td>
                  </tr>
                  <tr>
                    <td>Thương hiệu</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>{vehicle.brand}</td>
                    ))}
                  </tr>
                  <tr>
                    <td>Dòng xe</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>{vehicle.model}</td>
                    ))}
                  </tr>
                  <tr>
                    <td>Phiên bản</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>{vehicle.variant}</td>
                    ))}
                  </tr>
                  <tr>
                    <td>Giá bán</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId} className="price-cell">
                        {vehicle.price?.toLocaleString()} VNĐ
                      </td>
                    ))}
                  </tr>

                  {/* Performance */}
                  <tr className="section-header">
                    <td colSpan={selectedVehicles.length + 1}>Hiệu suất</td>
                  </tr>
                  <tr>
                    <td>Dung lượng pin</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Battery size={16} />
                          {vehicle.batteryCapacity || 'N/A'} kWh
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Tầm hoạt động</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Gauge size={16} />
                          {vehicle.range || 'N/A'} km
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Công suất</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Zap size={16} />
                          {vehicle.power || 'N/A'} kW
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Thời gian sạc (0-80%)</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Clock size={16} />
                          {vehicle.chargingTime || 'N/A'} phút
                        </div>
                      </td>
                    ))}
                  </tr>

                  {/* Dimensions */}
                  <tr className="section-header">
                    <td colSpan={selectedVehicles.length + 1}>Kích thước</td>
                  </tr>
                  <tr>
                    <td>Chiều dài</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Ruler size={16} />
                          {vehicle.length || 'N/A'} mm
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Chiều rộng</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Ruler size={16} />
                          {vehicle.width || 'N/A'} mm
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Chiều cao</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Ruler size={16} />
                          {vehicle.height || 'N/A'} mm
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Trọng lượng</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Scale size={16} />
                          {vehicle.weight || 'N/A'} kg
                        </div>
                      </td>
                    ))}
                  </tr>

                  {/* Capacity */}
                  <tr className="section-header">
                    <td colSpan={selectedVehicles.length + 1}>Sức chứa</td>
                  </tr>
                  <tr>
                    <td>Số chỗ ngồi</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Users size={16} />
                          {vehicle.seats || 'N/A'} chỗ
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Dung tích cốp</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="spec-cell">
                          <Car size={16} />
                          {vehicle.trunkCapacity || 'N/A'} L
                        </div>
                      </td>
                    ))}
                  </tr>

                  {/* Features */}
                  <tr className="section-header">
                    <td colSpan={selectedVehicles.length + 1}>Tính năng</td>
                  </tr>
                  <tr>
                    <td>Hệ thống lái tự động</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="feature-cell">
                          {vehicle.autopilot ? (
                            <Check className="check-icon" />
                          ) : (
                            <X className="x-icon" />
                          )}
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Hệ thống sạc nhanh</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="feature-cell">
                          {vehicle.fastCharging ? (
                            <Check className="check-icon" />
                          ) : (
                            <X className="x-icon" />
                          )}
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Hệ thống âm thanh cao cấp</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="feature-cell">
                          {vehicle.premiumAudio ? (
                            <Check className="check-icon" />
                          ) : (
                            <X className="x-icon" />
                          )}
                        </div>
                      </td>
                    ))}
                  </tr>
                  <tr>
                    <td>Hệ thống điều hòa tự động</td>
                    {selectedVehicles.map(vehicle => (
                      <td key={vehicle.vehicleId}>
                        <div className="feature-cell">
                          {vehicle.autoAC ? (
                            <Check className="check-icon" />
                          ) : (
                            <X className="x-icon" />
                          )}
                        </div>
                      </td>
                    ))}
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}

        {selectedVehicles.length === 0 && (
          <div className="empty-state">
            <Car size={64} />
            <h3>Chưa có xe nào được chọn</h3>
            <p>Hãy chọn ít nhất 2 xe để bắt đầu so sánh</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default VehicleComparison;
