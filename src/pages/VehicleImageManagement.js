import React, { useState, useEffect } from 'react';
import { Upload, Image as ImageIcon, RefreshCw, Eye } from 'lucide-react';
import { inventoryAPI } from '../services/api';
import ImageUploadModal from '../components/ImageUploadModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './VehicleImageManagement.css';

const VehicleImageManagement = () => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    try {
      setLoading(true);
      const response = await inventoryAPI.getInventory();
      setVehicles(response.data || []);
    } catch (error) {
      console.error('Error loading vehicles:', error);
      toast.error('Không thể tải danh sách xe');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadImage = (vehicle) => {
    setSelectedVehicle(vehicle);
    setShowUploadModal(true);
  };

  const handleUploadSuccess = (imageData) => {
    toast.success('Upload hình ảnh thành công!');
    loadVehicles(); // Reload to get updated data
  };

  const handleViewImage = (imageUrl) => {
    if (imageUrl) {
      window.open(`http://localhost:8080${imageUrl}`, '_blank');
    } else {
      toast.error('Xe này chưa có hình ảnh');
    }
  };

  const filteredVehicles = vehicles.filter(vehicle => {
    const searchLower = searchTerm.toLowerCase();
    return (
      (vehicle.variantName?.toLowerCase() || '').includes(searchLower) ||
      (vehicle.brand?.brandName?.toLowerCase() || '').includes(searchLower) ||
      (vehicle.model?.modelName?.toLowerCase() || '').includes(searchLower)
    );
  });

  const getImageStatus = (vehicle) => {
    if (vehicle.variant?.variantImageUrl) {
      return { status: 'has-image', text: 'Có hình ảnh', color: '#10b981' };
    }
    return { status: 'no-image', text: 'Chưa có hình', color: '#f59e0b' };
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Đang tải danh sách xe...</p>
      </div>
    );
  }

  return (
    <div className="vehicle-image-management">
      <div className="page-header">
        <h1 className="page-title">
          <ImageIcon className="page-icon" />
          Quản lý hình ảnh xe
        </h1>
        <p className="page-subtitle">Upload và quản lý hình ảnh cho các xe trong kho</p>
      </div>

      <div className="search-section">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Tìm kiếm xe theo tên, thương hiệu..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>
        <button className="refresh-btn" onClick={loadVehicles}>
          <RefreshCw size={16} />
          Làm mới
        </button>
      </div>

      <div className="vehicles-grid">
        {filteredVehicles.map(vehicle => {
          const imageStatus = getImageStatus(vehicle);
          return (
            <div key={vehicle.inventoryId} className="vehicle-card">
              <div className="vehicle-image">
                {vehicle.variant?.variantImageUrl ? (
                  <img 
                    src={`http://localhost:8080${vehicle.variant.variantImageUrl}`}
                    alt={vehicle.variantName}
                    onError={(e) => {
                      e.target.style.display = 'none';
                      e.target.nextSibling.style.display = 'flex';
                    }}
                  />
                ) : null}
                <div 
                  className="placeholder-image"
                  style={{ display: vehicle.variant?.variantImageUrl ? 'none' : 'flex' }}
                >
                  <ImageIcon className="placeholder-icon" />
                </div>
                
                <div className="image-status" style={{ backgroundColor: imageStatus.color }}>
                  {imageStatus.text}
                </div>
              </div>

              <div className="vehicle-info">
                <h3 className="vehicle-name">{vehicle.variantName}</h3>
                <p className="vehicle-brand">{vehicle.brand?.brandName} {vehicle.model?.modelName}</p>
                <p className="vehicle-price">
                  {(vehicle.variant?.priceBase || vehicle.priceBase || vehicle.price) 
                    ? `${(vehicle.variant?.priceBase || vehicle.priceBase || vehicle.price).toLocaleString('vi-VN')} VNĐ` 
                    : 'Liên hệ'}
                </p>
              </div>

              <div className="vehicle-actions">
                <button 
                  className="action-btn primary"
                  onClick={() => handleUploadImage(vehicle)}
                >
                  <Upload size={16} />
                  Upload hình
                </button>
                
                <button 
                  className="action-btn secondary"
                  onClick={() => handleViewImage(vehicle.variant?.variantImageUrl)}
                >
                  <Eye size={16} />
                  Xem hình
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {filteredVehicles.length === 0 && (
        <div className="empty-state">
          <ImageIcon className="empty-icon" />
          <h3>Không tìm thấy xe nào</h3>
          <p>Thử thay đổi từ khóa tìm kiếm</p>
        </div>
      )}

      <ImageUploadModal
        isOpen={showUploadModal}
        onClose={() => setShowUploadModal(false)}
        onSuccess={handleUploadSuccess}
        vehicleId={selectedVehicle?.inventoryId}
        vehicleName={selectedVehicle?.variantName}
      />
    </div>
  );
};

export default VehicleImageManagement;
