import React, { useState, useEffect } from 'react';
import { Plus, Search, Car, Zap, Edit, Trash2, Eye, Grid, List } from 'lucide-react';
import { inventoryAPI } from '../services/api';
import InventoryItemModal from '../components/modals/InventoryItemModal';
import CreateVehicleFromExistingModal from '../components/modals/CreateVehicleFromExistingModal';
import VehicleDetailModal from '../components/modals/VehicleDetailModal';
import VehicleImage from '../components/VehicleImage';
import '../styles/common.css';
import './VehicleManagementUnified.css';

// Utility function to get status display text
const getStatusDisplayText = (status) => {
  const statusMap = {
    'available': 'Có sẵn',
    'reserved': 'Đã đặt',
    'sold': 'Đã bán',
    'maintenance': 'Bảo trì',
    'damaged': 'Hư hỏng',
    'in_transit': 'Đang vận chuyển',
    'pending_delivery': 'Chờ giao hàng'
  };
  
  return statusMap[status] || status || 'Không xác định';
};

const VehicleManagementUnified = () => {
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'list'
  
  // Data states
  const [inventory, setInventory] = useState([]);
  
  // Modal states
  const [showInventoryModal, setShowInventoryModal] = useState(false);
  const [showCreateVehicleModal, setShowCreateVehicleModal] = useState(false);
  const [showVehicleDetailModal, setShowVehicleDetailModal] = useState(false);
  const [modalMode, setModalMode] = useState('create');
  const [selectedItem, setSelectedItem] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      
      // Try to get all inventory (including non-available status)
      let inventoryData = [];
      
      try {
        // Use regular inventory API
        const inventoryRes = await inventoryAPI.getInventory();
        inventoryData = inventoryRes.data || [];
        console.log('Inventory API response:', inventoryData);
        
        // Check if we got all statuses
        const statuses = [...new Set(inventoryData.map(item => item.status))];
        console.log('Statuses found:', statuses);
        
      } catch (error) {
        console.error('Error loading inventory:', error);
        inventoryData = [];
      }
      
      setInventory(inventoryData);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  // eslint-disable-next-line no-unused-vars
  const handleCreate = () => {
    setSelectedItem(null);
    setModalMode('create');
    setShowInventoryModal(true);
  };

  const handleCreateVehicle = () => {
    setShowCreateVehicleModal(true);
  };

  const handleView = (item) => {
    setSelectedItem(item);
    setModalMode('view');
    setShowVehicleDetailModal(true);
  };

  const handleEdit = (item) => {
    setSelectedItem(item);
    setModalMode('edit');
    setShowVehicleDetailModal(true);
  };

  const handleDelete = async (item) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa?')) {
      try {
        await inventoryAPI.deleteInventory(item.inventoryId);
        loadData();
      } catch (error) {
        console.error('Error deleting item:', error);
      }
    }
  };

  const getFilteredData = () => {
    let filtered = inventory;
    
    if (searchTerm) {
      filtered = filtered.filter(item => 
        Object.values(item).some(value => 
          value && value.toString().toLowerCase().includes(searchTerm.toLowerCase())
        )
      );
    }
    
    if (selectedBrand) {
      filtered = filtered.filter(item => 
        item.variant?.model?.brand?.brandName === selectedBrand
      );
    }
    
    return filtered;
  };

  const getUniqueBrands = () => {
    const brands = inventory
      .map(item => item.variant?.model?.brand?.brandName)
      .filter(Boolean);
    return [...new Set(brands)];
  };

  const renderVehicleCard = (vehicle) => (
    <div key={vehicle.inventoryId} className="vehicle-card">
      <div className="vehicle-image">
        <VehicleImage 
          vehicle={vehicle}
          className="card-image"
          size={24}
        />
      </div>
      <div className="vehicle-info">
        <div className="vehicle-header">
          <h3 className="vehicle-name">
            {vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName}
          </h3>
          <span className="vehicle-variant">{vehicle.variant?.variantName}</span>
        </div>
        
        <div className="vehicle-specs">
          <div className="spec-item">
            <Zap className="spec-icon" />
            <span>{vehicle.variant?.batteryCapacity || 'N/A'} kWh</span>
          </div>
          <div className="spec-item">
            <Car className="spec-icon" />
            <span>{vehicle.variant?.rangeKm || 'N/A'} km</span>
          </div>
        </div>
        
        <div className="vehicle-price">
          <span className="price-label">Giá bán:</span>
          <span className="price-value">
            {vehicle.sellingPrice?.toLocaleString('vi-VN') || 'Liên hệ'} VNĐ
          </span>
        </div>
        
        <div className="vehicle-status">
          <span className={`status-badge ${vehicle.status?.toLowerCase()}`}>
            {getStatusDisplayText(vehicle.status)}
          </span>
        </div>
        
        <div className="vehicle-actions">
          <button className="btn-icon" onClick={() => handleView(vehicle)} title="Xem">
            <Eye size={16} />
          </button>
          <button className="btn-icon" onClick={() => handleEdit(vehicle)} title="Sửa">
            <Edit size={16} />
          </button>
          <button className="btn-icon danger" onClick={() => handleDelete(vehicle)} title="Xóa">
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );

  const renderVehicleList = (vehicle) => (
    <div key={vehicle.inventoryId} className="vehicle-list-item">
      <div className="vehicle-list-image">
        <VehicleImage 
          vehicle={vehicle}
          className="list-image"
          size={24}
        />
      </div>
      <div className="vehicle-list-info">
        <div className="vehicle-list-header">
          <h3 className="vehicle-list-name">
            {vehicle.variant?.model?.brand?.brandName} {vehicle.variant?.model?.modelName} - {vehicle.variant?.variantName}
          </h3>
          <span className={`status-badge ${vehicle.status?.toLowerCase()}`}>
            {getStatusDisplayText(vehicle.status)}
          </span>
        </div>
        
        <div className="vehicle-list-specs">
          <div className="spec-item">
            <Zap className="spec-icon" />
            <span>{vehicle.variant?.batteryCapacity || 'N/A'} kWh</span>
          </div>
          <div className="spec-item">
            <Car className="spec-icon" />
            <span>{vehicle.variant?.rangeKm || 'N/A'} km</span>
          </div>
          <div className="spec-item">
            <span className="price-value">
              {vehicle.sellingPrice?.toLocaleString('vi-VN') || 'Liên hệ'} VNĐ
            </span>
          </div>
        </div>
        
        <div className="vehicle-list-actions">
          <button className="btn-icon" onClick={() => handleView(vehicle)} title="Xem">
            <Eye size={16} />
          </button>
          <button className="btn-icon" onClick={() => handleEdit(vehicle)} title="Sửa">
            <Edit size={16} />
          </button>
          <button className="btn-icon danger" onClick={() => handleDelete(vehicle)} title="Xóa">
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="vehicle-management-unified">
      <div className="page-header">
        <h1 className="page-title">Quản lý xe</h1>
        <p className="page-subtitle">Quản lý kho hàng xe điện</p>
      </div>

      <div className="content-section">
        <div className="section-header">
          <h2 className="section-title">Danh sách xe ({getFilteredData().length})</h2>
          <div className="section-actions">
            <div className="search-controls">
              <div className="search-bar">
                <Search className="search-icon" />
                <input
                  type="text"
                  placeholder="Tìm kiếm xe..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="search-input"
                />
              </div>
              <select
                value={selectedBrand}
                onChange={(e) => setSelectedBrand(e.target.value)}
                className="filter-select"
              >
                <option value="">Tất cả thương hiệu</option>
                {getUniqueBrands().map(brand => (
                  <option key={brand} value={brand}>{brand}</option>
                ))}
              </select>
            </div>
            <div className="view-controls">
              <div className="view-toggle">
                <button 
                  className={`view-btn ${viewMode === 'grid' ? 'active' : ''}`}
                  onClick={() => setViewMode('grid')}
                  title="Xem dạng lưới"
                >
                  <Grid size={18} />
                </button>
                <button 
                  className={`view-btn ${viewMode === 'list' ? 'active' : ''}`}
                  onClick={() => setViewMode('list')}
                  title="Xem dạng danh sách"
                >
                  <List size={18} />
                </button>
              </div>
              <div className="action-buttons">
                <button className="btn btn-primary" onClick={handleCreateVehicle}>
                  <Plus className="btn-icon" />
                  Tạo xe
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="data-section">
          {loading ? (
            <div className="loading-state">
              <div className="loading-spinner"></div>
              <p>Đang tải dữ liệu...</p>
            </div>
          ) : getFilteredData().length === 0 ? (
            <div className="empty-state">
              <Car className="empty-icon" />
              <h3>Không có xe nào</h3>
              <p>Hãy thêm xe mới vào kho hàng</p>
              <button className="btn btn-primary" onClick={handleCreateVehicle}>
                <Plus className="btn-icon" />
                Tạo xe đầu tiên
              </button>
            </div>
          ) : (
            <div className={`vehicle-container ${viewMode}`}>
              {getFilteredData().map(vehicle => 
                viewMode === 'grid' ? renderVehicleCard(vehicle) : renderVehicleList(vehicle)
              )}
            </div>
          )}
        </div>
      </div>

      {/* Modals */}
      <InventoryItemModal
        isOpen={showInventoryModal}
        onClose={() => setShowInventoryModal(false)}
        mode={modalMode}
        inventoryItem={selectedItem}
        onSave={() => {
          setShowInventoryModal(false);
          loadData();
        }}
      />

      <CreateVehicleFromExistingModal
        isOpen={showCreateVehicleModal}
        onClose={() => setShowCreateVehicleModal(false)}
        onSave={() => {
          setShowCreateVehicleModal(false);
          loadData();
        }}
      />

      <VehicleDetailModal
        isOpen={showVehicleDetailModal}
        onClose={() => setShowVehicleDetailModal(false)}
        mode={modalMode}
        inventoryItem={selectedItem}
        onSave={() => {
          setShowVehicleDetailModal(false);
          loadData();
        }}
      />
    </div>
  );
};

export default VehicleManagementUnified;
