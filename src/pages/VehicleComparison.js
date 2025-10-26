import React, { useState, useEffect } from 'react';
import { 
  Search, 
  Filter, 
  BarChart3, 
  Table, 
  Grid, 
  List, 
  X, 
  RefreshCw,
  Plus,
  Trash2,
  Star,
  Award,
  TrendingUp
} from 'lucide-react';
import { publicVehicleComparisonAPI } from '../services/api';
import VehicleComparisonCard from '../components/VehicleComparisonCard';
import ComparisonTable from '../components/ComparisonTable';
import ComparisonChart from '../components/ComparisonChart';
import toast from 'react-hot-toast';
import './VehicleComparison.css';

const VehicleComparison = () => {
  const [vehicles, setVehicles] = useState([]);
  const [filteredVehicles, setFilteredVehicles] = useState([]);
  const [selectedVehicles, setSelectedVehicles] = useState([]);
  const [comparisonData, setComparisonData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState('grid'); // grid, list, table, chart
  const [showComparison, setShowComparison] = useState(false);
  const [filters, setFilters] = useState({
    brand: '',
    priceRange: { min: 0, max: 10000000000 },
    range: { min: 0, max: 1000 },
    power: { min: 0, max: 1000 }
  });

  useEffect(() => {
    loadVehicles();
  }, []);

  useEffect(() => {
    filterVehicles();
  }, [vehicles, searchTerm, filters]);

  const loadVehicles = async () => {
    try {
      setLoading(true);
      const response = await publicVehicleComparisonAPI.getAvailableForCompare();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading vehicles:', error);
      toast.error('Không thể tải danh sách xe');
    } finally {
      setLoading(false);
    }
  };

  const filterVehicles = () => {
    let filtered = vehicles.filter(vehicle => {
      const matchesSearch = 
        (vehicle.variantName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
        (vehicle.model?.modelName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
        (vehicle.model?.brand?.brandName?.toLowerCase() || '').includes(searchTerm.toLowerCase());

      const matchesBrand = !filters.brand || 
        vehicle.model?.brand?.brandName === filters.brand;

      const matchesPrice = 
        vehicle.basePrice >= filters.priceRange.min && 
        vehicle.basePrice <= filters.priceRange.max;

      const matchesRange = 
        (vehicle.range || 0) >= filters.range.min && 
        (vehicle.range || 0) <= filters.range.max;

      const matchesPower = 
        (vehicle.powerKw || 0) >= filters.power.min && 
        (vehicle.powerKw || 0) <= filters.power.max;

      return matchesSearch && matchesBrand && matchesPrice && matchesRange && matchesPower;
    });

    setFilteredVehicles(filtered);
  };

  const handleSelectVehicle = (variantId) => {
    if (selectedVehicles.includes(variantId)) {
      setSelectedVehicles(selectedVehicles.filter(id => id !== variantId));
    } else if (selectedVehicles.length < 5) {
      setSelectedVehicles([...selectedVehicles, variantId]);
    } else {
      toast.error('Chỉ có thể so sánh tối đa 5 xe');
    }
  };

  const handleRemoveVehicle = (variantId) => {
    setSelectedVehicles(selectedVehicles.filter(id => id !== variantId));
    if (selectedVehicles.length === 1) {
      setShowComparison(false);
      setComparisonData(null);
    }
  };

  const handleCompare = async () => {
    if (selectedVehicles.length < 2) {
      toast.error('Vui lòng chọn ít nhất 2 xe để so sánh');
      return;
    }

    try {
      setLoading(true);
      const response = await publicVehicleComparisonAPI.detailedCompare({
        variantIds: selectedVehicles,
        comparisonCriteria: ['price', 'range', 'power', 'batteryCapacity', 'chargingTime'],
        includeDetails: true,
        includePricing: true,
        includeAvailability: true
      });

      setComparisonData(response.data);
      setShowComparison(true);
      toast.success('So sánh xe thành công!');
    } catch (error) {
      console.error('Error comparing vehicles:', error);
      toast.error('Không thể so sánh xe');
    } finally {
      setLoading(false);
    }
  };

  const clearComparison = () => {
    setSelectedVehicles([]);
    setShowComparison(false);
    setComparisonData(null);
  };

  const getSelectedVehiclesData = () => {
    return selectedVehicles.map(id => {
      const vehicle = vehicles.find(v => v.variantId === id);
      const comparison = comparisonData?.vehicles?.find(v => v.variantId === id);
      return { ...vehicle, ...comparison };
    });
  };

  const getBrands = () => {
    const brands = [...new Set(vehicles.map(v => v.model?.brand?.brandName).filter(Boolean))];
    return brands.sort();
  };

  const formatPrice = (price) => {
    if (!price) return 'Liên hệ';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(price);
  };

  if (loading && vehicles.length === 0) {
    return (
      <div className="vehicle-comparison-loading">
        <div className="loading-spinner">
          <RefreshCw size={32} className="spinning" />
        </div>
        <p>Đang tải danh sách xe...</p>
      </div>
    );
  }

  return (
    <div className="vehicle-comparison">
      <div className="comparison-header">
        <div className="header-content">
          <h1>So sánh xe điện</h1>
          <p>Tìm và so sánh các mẫu xe điện phù hợp với nhu cầu của bạn</p>
        </div>
        
        {selectedVehicles.length > 0 && (
          <div className="comparison-actions">
            <div className="selected-count">
              <span>{selectedVehicles.length} xe đã chọn</span>
            </div>
            <button 
              className="compare-button"
              onClick={handleCompare}
              disabled={selectedVehicles.length < 2}
            >
              <BarChart3 size={16} />
              So sánh ({selectedVehicles.length})
            </button>
            <button 
              className="clear-button"
              onClick={clearComparison}
            >
              <X size={16} />
              Xóa tất cả
            </button>
          </div>
        )}
      </div>

      <div className="comparison-content">
        {!showComparison ? (
          <>
            {/* Search and Filters */}
            <div className="search-filters">
              <div className="search-bar">
                <Search size={20} />
                <input
                  type="text"
                  placeholder="Tìm kiếm xe theo tên, thương hiệu..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              <div className="filters">
                <div className="filter-group">
                  <label>Thương hiệu:</label>
                  <select
                    value={filters.brand}
                    onChange={(e) => setFilters({...filters, brand: e.target.value})}
                  >
                    <option value="">Tất cả</option>
                    {getBrands().map(brand => (
                      <option key={brand} value={brand}>{brand}</option>
                    ))}
                  </select>
                </div>

                <div className="filter-group">
                  <label>Khoảng giá:</label>
                  <div className="range-inputs">
                    <input
                      type="number"
                      placeholder="Từ"
                      value={filters.priceRange.min}
                      onChange={(e) => setFilters({
                        ...filters, 
                        priceRange: {...filters.priceRange, min: parseInt(e.target.value) || 0}
                      })}
                    />
                    <span>-</span>
                    <input
                      type="number"
                      placeholder="Đến"
                      value={filters.priceRange.max}
                      onChange={(e) => setFilters({
                        ...filters, 
                        priceRange: {...filters.priceRange, max: parseInt(e.target.value) || 10000000000}
                      })}
                    />
                  </div>
                </div>

                <div className="filter-group">
                  <label>Tầm hoạt động (km):</label>
                  <div className="range-inputs">
                    <input
                      type="number"
                      placeholder="Từ"
                      value={filters.range.min}
                      onChange={(e) => setFilters({
                        ...filters, 
                        range: {...filters.range, min: parseInt(e.target.value) || 0}
                      })}
                    />
                    <span>-</span>
                    <input
                      type="number"
                      placeholder="Đến"
                      value={filters.range.max}
                      onChange={(e) => setFilters({
                        ...filters, 
                        range: {...filters.range, max: parseInt(e.target.value) || 1000}
                      })}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* View Mode Toggle */}
            <div className="view-controls">
              <div className="view-modes">
                <button 
                  className={`view-mode ${viewMode === 'grid' ? 'active' : ''}`}
                  onClick={() => setViewMode('grid')}
                >
                  <Grid size={16} />
                  Lưới
                </button>
                <button 
                  className={`view-mode ${viewMode === 'list' ? 'active' : ''}`}
                  onClick={() => setViewMode('list')}
                >
                  <List size={16} />
                  Danh sách
                </button>
              </div>

              <div className="results-count">
                {filteredVehicles.length} xe tìm thấy
              </div>
            </div>

            {/* Vehicle List */}
            <div className={`vehicles-container ${viewMode}`}>
              {filteredVehicles.map(vehicle => (
                <VehicleComparisonCard
                  key={vehicle.variantId}
                  vehicle={vehicle}
                  isSelected={selectedVehicles.includes(vehicle.variantId)}
                  onSelect={handleSelectVehicle}
                  showRemoveButton={false}
                />
              ))}
            </div>

            {filteredVehicles.length === 0 && (
              <div className="no-results">
                <div className="no-results-icon">
                  <Search size={48} />
                </div>
                <h3>Không tìm thấy xe phù hợp</h3>
                <p>Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                <button 
                  className="reset-filters"
                  onClick={() => {
                    setSearchTerm('');
                    setFilters({
                      brand: '',
                      priceRange: { min: 0, max: 10000000000 },
                      range: { min: 0, max: 1000 },
                      power: { min: 0, max: 1000 }
                    });
                  }}
                >
                  <RefreshCw size={16} />
                  Đặt lại bộ lọc
                </button>
              </div>
            )}
          </>
        ) : (
          <>
            {/* Comparison Results */}
            <div className="comparison-results">
              <div className="results-header">
                <h2>Kết quả so sánh</h2>
                <div className="results-actions">
                  <div className="view-mode-tabs">
                    <button 
                      className={`tab ${viewMode === 'grid' ? 'active' : ''}`}
                      onClick={() => setViewMode('grid')}
                    >
                      <Grid size={16} />
                      Thẻ
                    </button>
                    <button 
                      className={`tab ${viewMode === 'table' ? 'active' : ''}`}
                      onClick={() => setViewMode('table')}
                    >
                      <Table size={16} />
                      Bảng
                    </button>
                    <button 
                      className={`tab ${viewMode === 'chart' ? 'active' : ''}`}
                      onClick={() => setViewMode('chart')}
                    >
                      <BarChart3 size={16} />
                      Biểu đồ
                    </button>
                  </div>
                </div>
              </div>

              {viewMode === 'grid' && (
                <div className="comparison-cards">
                  {getSelectedVehiclesData().map((vehicle, index) => (
                    <VehicleComparisonCard
                      key={vehicle.variantId}
                      vehicle={vehicle}
                      comparisonData={vehicle}
                      rank={vehicle.rank}
                      showRemoveButton={true}
                      onRemove={handleRemoveVehicle}
                    />
                  ))}
                </div>
              )}

              {viewMode === 'table' && (
                <ComparisonTable 
                  comparisonData={comparisonData}
                  vehicles={getSelectedVehiclesData()}
                />
              )}

              {viewMode === 'chart' && (
                <ComparisonChart 
                  comparisonData={comparisonData}
                  vehicles={getSelectedVehiclesData()}
                />
              )}

              {comparisonData && (
                <div className="comparison-summary">
                  <h3>Tóm tắt so sánh</h3>
                  <div className="summary-grid">
                    <div className="summary-item">
                      <div className="summary-icon">
                        <Award size={20} />
                      </div>
                      <div className="summary-content">
                        <h4>Xe tốt nhất</h4>
                        <p>{comparisonData.bestVehicle?.variantName}</p>
                        <span className="summary-score">{comparisonData.bestVehicle?.overallScore} điểm</span>
                      </div>
                    </div>
                    
                    <div className="summary-item">
                      <div className="summary-icon">
                        <TrendingUp size={20} />
                      </div>
                      <div className="summary-content">
                        <h4>Giá trung bình</h4>
                        <p>{formatPrice(comparisonData.priceRange?.average)}</p>
                        <span className="summary-range">
                          {formatPrice(comparisonData.priceRange?.min)} - {formatPrice(comparisonData.priceRange?.max)}
                        </span>
                      </div>
                    </div>
                    
                    <div className="summary-item">
                      <div className="summary-icon">
                        <Star size={20} />
                      </div>
                      <div className="summary-content">
                        <h4>Điểm trung bình</h4>
                        <p>{Math.round(comparisonData.averageScore || 0)} điểm</p>
                        <span className="summary-range">
                          {comparisonData.minScore || 0} - {comparisonData.maxScore || 0}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default VehicleComparison;