import React, { useState, useEffect } from 'react';
import { X, Save, Car, Zap, Package, Image as ImageIcon, Star, DollarSign } from 'lucide-react';
import { inventoryAPI, warehouseAPI } from '../../services/api';
import VehicleImage from '../VehicleImage';
import './Modal.css';

const VehicleDetailModal = ({ isOpen, onClose, onSave, mode = 'view', inventoryItem }) => {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Data states
  const [warehouses, setWarehouses] = useState([]);
  const [images, setImages] = useState([]);
  
  // Form data
  const [formData, setFormData] = useState({
    // Inventory fields
    vin: '',
    chassisNumber: '',
    sellingPrice: '',
    status: 'AVAILABLE',
    notes: '',
    warehouseId: '',
    warehouseLocation: '',
    
    // Vehicle info (read-only)
    brandName: '',
    modelName: '',
    variantName: '',
    colorName: '',
    batteryCapacity: '',
    rangeKm: '',
    powerKw: '',
    acceleration0100: '',
    topSpeed: '',
    chargingTimeFast: '',
    chargingTimeSlow: '',
    priceBase: ''
  });

  useEffect(() => {
    if (isOpen && inventoryItem) {
      loadData();
      loadItemDetails();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, inventoryItem]);

  const loadData = async () => {
    try {
      const warehousesRes = await warehouseAPI.getActiveWarehouses();
      setWarehouses(warehousesRes.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };

  const loadItemDetails = async () => {
    try {
      setLoading(true);
      const response = await inventoryAPI.getInventoryById(inventoryItem.inventoryId);
      const item = response.data;
      
      // Parse vehicle images from JSON string
      parseVehicleImages(item);
      
      setFormData({
        vin: item.vin || '',
        chassisNumber: item.chassisNumber || '',
        sellingPrice: item.sellingPrice || '',
        status: item.status || 'AVAILABLE',
        notes: item.notes || '',
        warehouseId: item.warehouse?.warehouseId || '',
        warehouseLocation: item.warehouseLocation || '',
        
        // Vehicle info (read-only)
        brandName: item.variant?.model?.brand?.brandName || '',
        modelName: item.variant?.model?.modelName || '',
        variantName: item.variant?.variantName || '',
        colorName: item.color?.colorName || '',
        batteryCapacity: item.variant?.batteryCapacity || '',
        rangeKm: item.variant?.rangeKm || '',
        powerKw: item.variant?.powerKw || '',
        acceleration0100: item.variant?.acceleration0100 || '',
        topSpeed: item.variant?.topSpeed || '',
        chargingTimeFast: item.variant?.chargingTimeFast || '',
        chargingTimeSlow: item.variant?.chargingTimeSlow || '',
        priceBase: item.variant?.priceBase || ''
      });
    } catch (error) {
      console.error('Error loading item details:', error);
      setError('Không thể tải thông tin chi tiết');
    } finally {
      setLoading(false);
    }
  };

  const parseVehicleImages = (item) => {
    try {
      if (!item.vehicleImages) {
        console.log('No vehicleImages found in item:', item);
        setImages([]);
        return;
      }

      console.log('Raw vehicleImages:', item.vehicleImages);
      const vehicleImages = JSON.parse(item.vehicleImages || '{}');
      console.log('Parsed vehicleImages:', vehicleImages);
      const imageList = [];

      // Parse main images
      if (vehicleImages.main) {
        if (Array.isArray(vehicleImages.main)) {
          vehicleImages.main.forEach((url, index) => {
            imageList.push({
              imageUrl: url,
              category: 'main',
              filename: `main_${index + 1}.jpg`
            });
          });
        } else {
          imageList.push({
            imageUrl: vehicleImages.main,
            category: 'main',
            filename: 'main.jpg'
          });
        }
      }

      // Parse interior images
      if (vehicleImages.interior) {
        if (Array.isArray(vehicleImages.interior)) {
          vehicleImages.interior.forEach((url, index) => {
            imageList.push({
              imageUrl: url,
              category: 'interior',
              filename: `interior_${index + 1}.jpg`
            });
          });
        } else {
          imageList.push({
            imageUrl: vehicleImages.interior,
            category: 'interior',
            filename: 'interior.jpg'
          });
        }
      }

      // Parse exterior images
      if (vehicleImages.exterior) {
        console.log('Found exterior images:', vehicleImages.exterior);
        if (Array.isArray(vehicleImages.exterior)) {
          vehicleImages.exterior.forEach((url, index) => {
            console.log(`Adding exterior image ${index + 1}:`, url);
            imageList.push({
              imageUrl: url,
              category: 'exterior',
              filename: `exterior_${index + 1}.jpg`
            });
          });
        } else {
          console.log('Adding single exterior image:', vehicleImages.exterior);
          imageList.push({
            imageUrl: vehicleImages.exterior,
            category: 'exterior',
            filename: 'exterior.jpg'
          });
        }
      } else {
        console.log('No exterior images found');
      }

      console.log('Final imageList:', imageList);
      setImages(imageList);
    } catch (error) {
      console.error('Error parsing vehicle images:', error);
      setImages([]);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (mode === 'view') return;

    setSaving(true);
    setError('');
    setSuccess('');

    try {
      const updateData = {
        vin: formData.vin,
        chassisNumber: formData.chassisNumber,
        sellingPrice: formData.sellingPrice,
        status: formData.status,
        notes: formData.notes,
        warehouseId: formData.warehouseId,
        warehouseLocation: formData.warehouseLocation
      };

      await inventoryAPI.updateInventory(inventoryItem.inventoryId, updateData);
      
      setSuccess('Cập nhật thành công!');
      setTimeout(() => {
        onSave();
        onClose();
      }, 1500);
      
    } catch (error) {
      console.error('Error updating inventory:', error);
      setError(error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật');
    } finally {
      setSaving(false);
    }
  };

  const renderInfoSection = (title, Icon, fields) => (
    <div className="info-section">
      <div className="info-section-header">
        <Icon className="info-section-icon" />
        <h3 className="info-section-title">{title}</h3>
      </div>
      <div className="info-section-content">
        {fields.map(field => (
          <div key={field.name} className="info-item">
            <label className="info-label">{field.label}</label>
            {field.type === 'select' ? (
              <select
                name={field.name}
                value={formData[field.name]}
                onChange={handleInputChange}
                className="info-input"
                disabled={mode === 'view'}
              >
                <option value="">Chọn {field.label.toLowerCase()}</option>
                {field.options?.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            ) : field.type === 'textarea' ? (
              <textarea
                name={field.name}
                value={formData[field.name]}
                onChange={handleInputChange}
                className="info-input"
                rows={3}
                disabled={mode === 'view'}
                placeholder={field.placeholder}
              />
            ) : (
              <input
                type={field.type}
                name={field.name}
                value={formData[field.name]}
                onChange={handleInputChange}
                className={`info-input ${field.readOnly ? 'read-only' : ''}`}
                disabled={mode === 'view' || field.readOnly}
                placeholder={field.placeholder}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container vehicle-detail-modal">
        <div className="modal-header">
          <h2 className="modal-title">
            <Car className="modal-title-icon" />
            {mode === 'view' ? 'Chi tiết xe' : 'Chỉnh sửa xe'}
          </h2>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-content">
          {error && (
            <div className="alert alert-error">
              <X className="alert-icon" />
              {error}
            </div>
          )}

          {success && (
            <div className="alert alert-success">
              <Save className="alert-icon" />
              {success}
            </div>
          )}

          {loading ? (
            <div className="loading-state">
              <div className="loading-spinner"></div>
              <p>Đang tải thông tin...</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="vehicle-detail-form">
              <div className="vehicle-overview">
                <div className="vehicle-image-section">
                  <VehicleImage 
                    vehicle={inventoryItem}
                    className="main-vehicle-image"
                    size={48}
                  />
                  <div className="vehicle-basic-info">
                    <h2 className="vehicle-title">
                      {formData.brandName} {formData.modelName}
                    </h2>
                    <p className="vehicle-subtitle">{formData.variantName}</p>
                    <div className="vehicle-status-badge">
                      <span className={`status-badge ${formData.status?.toLowerCase()}`}>
                        {formData.status === 'AVAILABLE' ? 'Có sẵn' : 
                         formData.status === 'RESERVED' ? 'Đã đặt' :
                         formData.status === 'SOLD' ? 'Đã bán' : 'Bảo trì'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="vehicle-details">
                {renderInfoSection(
                  'Thông tin thương hiệu & dòng xe',
                  Star,
                  [
                    { name: 'brandName', label: 'Thương hiệu', type: 'text', readOnly: true },
                    { name: 'modelName', label: 'Dòng xe', type: 'text', readOnly: true },
                    { name: 'variantName', label: 'Phiên bản', type: 'text', readOnly: true },
                    { name: 'colorName', label: 'Màu sắc', type: 'text', readOnly: true }
                  ]
                )}

                {renderInfoSection(
                  'Thông số kỹ thuật',
                  Zap,
                  [
                    { name: 'batteryCapacity', label: 'Dung lượng pin (kWh)', type: 'text', readOnly: true },
                    { name: 'rangeKm', label: 'Tầm hoạt động (km)', type: 'text', readOnly: true },
                    { name: 'powerKw', label: 'Công suất (kW)', type: 'text', readOnly: true },
                    { name: 'acceleration0100', label: 'Tăng tốc 0-100km/h (s)', type: 'text', readOnly: true },
                    { name: 'topSpeed', label: 'Tốc độ tối đa (km/h)', type: 'text', readOnly: true },
                    { name: 'chargingTimeFast', label: 'Thời gian sạc nhanh (phút)', type: 'text', readOnly: true },
                    { name: 'chargingTimeSlow', label: 'Thời gian sạc chậm (giờ)', type: 'text', readOnly: true }
                  ]
                )}

                {renderInfoSection(
                  'Thông tin giá cả',
                  DollarSign,
                  [
                    { name: 'priceBase', label: 'Giá bán cơ bản (VNĐ)', type: 'text', readOnly: true },
                    { name: 'sellingPrice', label: 'Giá bán thực tế (VNĐ)', type: 'number', placeholder: 'Nhập giá bán thực tế' }
                  ]
                )}

                {renderInfoSection(
                  'Thông tin kho hàng',
                  Package,
                  [
                    { name: 'vin', label: 'Số VIN', type: 'text', placeholder: 'Nhập số VIN' },
                    { name: 'chassisNumber', label: 'Số khung', type: 'text', placeholder: 'Nhập số khung' },
                    { name: 'warehouseId', label: 'Kho chứa', type: 'select', options: warehouses.map(w => ({ value: w.warehouseId, label: w.warehouseName })) },
                    { name: 'warehouseLocation', label: 'Vị trí trong kho', type: 'text', placeholder: 'VD: A-01-15' },
                    { name: 'status', label: 'Trạng thái', type: 'select', options: [
                      { value: 'AVAILABLE', label: 'Có sẵn' },
                      { value: 'RESERVED', label: 'Đã đặt' },
                      { value: 'SOLD', label: 'Đã bán' },
                      { value: 'MAINTENANCE', label: 'Bảo trì' }
                    ]},
                    { name: 'notes', label: 'Ghi chú', type: 'textarea', placeholder: 'Ghi chú về xe...' }
                  ]
                )}

                {images.length > 0 && (
                  <div className="info-section">
                    <div className="info-section-header">
                      <ImageIcon className="info-section-icon" />
                      <h3 className="info-section-title">Hình ảnh xe ({images.length})</h3>
                    </div>
                    <div className="vehicle-images-grid">
                      {images.map((image, index) => (
                        <div key={index} className="vehicle-image-item">
                          <img 
                            src={`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}${image.imageUrl}`}
                            alt={`Hình ảnh ${index + 1}`}
                            className="vehicle-thumbnail"
                            onError={(e) => {
                              console.log('Image load error:', image.imageUrl);
                              e.target.style.display = 'none';
                              e.target.nextElementSibling.style.display = 'flex';
                            }}
                            onLoad={() => console.log('Image loaded successfully:', image.imageUrl)}
                          />
                          <div className="image-placeholder" style={{ display: 'none' }}>
                            <ImageIcon className="placeholder-icon" size={32} />
                            <span className="placeholder-text">Không có hình ảnh</span>
                          </div>
                          <div className="image-info">
                            <span className="image-category">{image.category}</span>
                            <span className="image-filename">{image.filename}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                  {mode === 'view' ? 'Đóng' : 'Hủy'}
                </button>
                {mode === 'edit' && (
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    <Save className="btn-icon" />
                    {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
                  </button>
                )}
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default VehicleDetailModal;
