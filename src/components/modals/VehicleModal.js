import React, { useState, useEffect } from 'react';
import { X, Save, Car, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleModal = ({ vehicle, isOpen, onClose, onSave, mode = 'view', vehicleType = 'brand' }) => {
  const [formData, setFormData] = useState({
    // Brand fields
    brandName: '',
    brandCode: '',
    country: '',
    description: '',
    isActive: true,
    
    // Model fields
    modelName: '',
    modelCode: '',
    brandId: '',
    year: new Date().getFullYear(),
    bodyType: 'SEDAN',
    
    // Variant fields
    variantName: '',
    variantCode: '',
    modelId: '',
    engineType: 'ELECTRIC',
    batteryCapacity: 0,
    range: 0,
    chargingTime: 0,
    maxSpeed: 0,
    acceleration: 0,
    price: 0,
    
    // Color fields
    colorName: '',
    colorCode: '',
    hexCode: '#000000',
    isMetallic: false
  });
  const [loading, setLoading] = useState(false);
  const [brands, setBrands] = useState([]);
  const [models, setModels] = useState([]);

  useEffect(() => {
    if (isOpen) {
      loadBrands();
      if (vehicleType === 'model' || vehicleType === 'variant') {
        loadModels();
      }
      
      if (mode === 'create') {
        setFormData({
          brandName: '',
          brandCode: '',
          country: '',
          description: '',
          isActive: true,
          modelName: '',
          modelCode: '',
          brandId: '',
          year: new Date().getFullYear(),
          bodyType: 'SEDAN',
          variantName: '',
          variantCode: '',
          modelId: '',
          engineType: 'ELECTRIC',
          batteryCapacity: 0,
          range: 0,
          chargingTime: 0,
          maxSpeed: 0,
          acceleration: 0,
          price: 0,
          colorName: '',
          colorCode: '',
          hexCode: '#000000',
          isMetallic: false
        });
      } else if (vehicle) {
        setFormData({
          brandName: vehicle.brandName || '',
          brandCode: vehicle.brandCode || '',
          country: vehicle.country || '',
          description: vehicle.description || '',
          isActive: vehicle.isActive !== undefined ? vehicle.isActive : true,
          modelName: vehicle.modelName || '',
          modelCode: vehicle.modelCode || '',
          brandId: vehicle.brand?.brandId || vehicle.brandId || '',
          year: vehicle.year || new Date().getFullYear(),
          bodyType: vehicle.bodyType || 'SEDAN',
          variantName: vehicle.variantName || '',
          variantCode: vehicle.variantCode || '',
          modelId: vehicle.model?.modelId || vehicle.modelId || '',
          engineType: vehicle.engineType || 'ELECTRIC',
          batteryCapacity: vehicle.batteryCapacity || 0,
          range: vehicle.range || 0,
          chargingTime: vehicle.chargingTime || 0,
          maxSpeed: vehicle.maxSpeed || 0,
          acceleration: vehicle.acceleration || 0,
          price: vehicle.price || 0,
          colorName: vehicle.colorName || '',
          colorCode: vehicle.colorCode || '',
          hexCode: vehicle.hexCode || '#000000',
          isMetallic: vehicle.isMetallic || false
        });
      }
    }
  }, [isOpen, vehicle, mode, vehicleType]);

  const loadBrands = async () => {
    try {
      const response = await vehicleAPI.getBrands();
      setBrands(response.data || []);
    } catch (error) {
      console.error('Error loading brands:', error);
    }
  };

  const loadModels = async () => {
    try {
      const response = await vehicleAPI.getModels();
      setModels(response.data || []);
    } catch (error) {
      console.error('Error loading models:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    // Validation based on vehicle type
    if (vehicleType === 'brand') {
      if (!formData.brandName.trim()) {
        toast.error('Tên thương hiệu là bắt buộc');
        return;
      }
      if (!formData.brandCode.trim()) {
        toast.error('Mã thương hiệu là bắt buộc');
        return;
      }
    } else if (vehicleType === 'model') {
      if (!formData.modelName.trim()) {
        toast.error('Tên dòng xe là bắt buộc');
        return;
      }
      if (!formData.modelCode.trim()) {
        toast.error('Mã dòng xe là bắt buộc');
        return;
      }
      if (!formData.brandId) {
        toast.error('Vui lòng chọn thương hiệu');
        return;
      }
    } else if (vehicleType === 'variant') {
      if (!formData.variantName.trim()) {
        toast.error('Tên phiên bản là bắt buộc');
        return;
      }
      if (!formData.variantCode.trim()) {
        toast.error('Mã phiên bản là bắt buộc');
        return;
      }
      if (!formData.modelId) {
        toast.error('Vui lòng chọn dòng xe');
        return;
      }
      if (formData.price <= 0) {
        toast.error('Giá xe phải lớn hơn 0');
        return;
      }
    } else if (vehicleType === 'color') {
      if (!formData.colorName.trim()) {
        toast.error('Tên màu sắc là bắt buộc');
        return;
      }
      if (!formData.colorCode.trim()) {
        toast.error('Mã màu sắc là bắt buộc');
        return;
      }
    }

    try {
      setLoading(true);
      
      let submitData = {};
      
      if (vehicleType === 'brand') {
        submitData = {
          brandName: formData.brandName.trim(),
          brandCode: formData.brandCode.trim(),
          country: formData.country.trim(),
          description: formData.description.trim(),
          isActive: formData.isActive
        };
      } else if (vehicleType === 'model') {
        submitData = {
          modelName: formData.modelName.trim(),
          modelCode: formData.modelCode.trim(),
          brandId: formData.brandId,
          year: parseInt(formData.year),
          bodyType: formData.bodyType,
          isActive: formData.isActive
        };
      } else if (vehicleType === 'variant') {
        submitData = {
          variantName: formData.variantName.trim(),
          variantCode: formData.variantCode.trim(),
          modelId: formData.modelId,
          engineType: formData.engineType,
          batteryCapacity: parseFloat(formData.batteryCapacity),
          range: parseInt(formData.range),
          chargingTime: parseFloat(formData.chargingTime),
          maxSpeed: parseInt(formData.maxSpeed),
          acceleration: parseFloat(formData.acceleration),
          price: parseFloat(formData.price),
          isActive: formData.isActive
        };
      } else if (vehicleType === 'color') {
        submitData = {
          colorName: formData.colorName.trim(),
          colorCode: formData.colorCode.trim(),
          hexCode: formData.hexCode,
          isMetallic: formData.isMetallic,
          isActive: formData.isActive
        };
      }

      console.log('Sending vehicle data:', submitData);
      
      if (mode === 'create') {
        await onSave(null, submitData);
      } else {
        await onSave(vehicle[`${vehicleType}Id`], submitData);
      }
    } catch (error) {
      console.error('Error saving vehicle:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || error.message;
        toast.error(`Lỗi: ${errorMessage}`);
      } else {
        toast.error(mode === 'create' ? 'Không thể tạo dữ liệu xe' : 'Không thể cập nhật dữ liệu xe');
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const getTitle = () => {
    const typeNames = {
      brand: 'thương hiệu',
      model: 'dòng xe',
      variant: 'phiên bản',
      color: 'màu sắc'
    };
    const typeName = typeNames[vehicleType] || 'xe';
    
    if (mode === 'view') return `Xem chi tiết ${typeName}`;
    if (mode === 'create') return `Thêm ${typeName} mới`;
    return `Chỉnh sửa ${typeName}`;
  };

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <Car size={24} />
            <h2>{getTitle()}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            {vehicleType === 'brand' && (
              <>
                <div className="form-group">
                  <label htmlFor="brandName">Tên thương hiệu *</label>
                  <input
                    type="text"
                    id="brandName"
                    name="brandName"
                    value={formData.brandName}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tên thương hiệu"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="brandCode">Mã thương hiệu *</label>
                  <input
                    type="text"
                    id="brandCode"
                    name="brandCode"
                    value={formData.brandCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã thương hiệu"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="country">Quốc gia</label>
                  <input
                    type="text"
                    id="country"
                    name="country"
                    value={formData.country}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập quốc gia"
                  />
                </div>
              </>
            )}

            {vehicleType === 'model' && (
              <>
                <div className="form-group">
                  <label htmlFor="modelName">Tên dòng xe *</label>
                  <input
                    type="text"
                    id="modelName"
                    name="modelName"
                    value={formData.modelName}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tên dòng xe"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="modelCode">Mã dòng xe *</label>
                  <input
                    type="text"
                    id="modelCode"
                    name="modelCode"
                    value={formData.modelCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã dòng xe"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="brandId">Thương hiệu *</label>
                  <select
                    id="brandId"
                    name="brandId"
                    value={formData.brandId}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="">Chọn thương hiệu</option>
                    {brands.map((brand) => (
                      <option key={brand.brandId} value={brand.brandId}>
                        {brand.brandName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="year">Năm sản xuất</label>
                  <input
                    type="number"
                    id="year"
                    name="year"
                    value={formData.year}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="1900"
                    max="2030"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="bodyType">Loại thân xe</label>
                  <select
                    id="bodyType"
                    name="bodyType"
                    value={formData.bodyType}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="SEDAN">Sedan</option>
                    <option value="SUV">SUV</option>
                    <option value="HATCHBACK">Hatchback</option>
                    <option value="COUPE">Coupe</option>
                    <option value="CONVERTIBLE">Convertible</option>
                    <option value="WAGON">Wagon</option>
                    <option value="PICKUP">Pickup</option>
                  </select>
                </div>
              </>
            )}

            {vehicleType === 'variant' && (
              <>
                <div className="form-group">
                  <label htmlFor="variantName">Tên phiên bản *</label>
                  <input
                    type="text"
                    id="variantName"
                    name="variantName"
                    value={formData.variantName}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tên phiên bản"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="variantCode">Mã phiên bản *</label>
                  <input
                    type="text"
                    id="variantCode"
                    name="variantCode"
                    value={formData.variantCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã phiên bản"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="modelId">Dòng xe *</label>
                  <select
                    id="modelId"
                    name="modelId"
                    value={formData.modelId}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="">Chọn dòng xe</option>
                    {models.map((model) => (
                      <option key={model.modelId} value={model.modelId}>
                        {model.modelName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="engineType">Loại động cơ</label>
                  <select
                    id="engineType"
                    name="engineType"
                    value={formData.engineType}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="ELECTRIC">Điện</option>
                    <option value="HYBRID">Hybrid</option>
                    <option value="PETROL">Xăng</option>
                    <option value="DIESEL">Diesel</option>
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="batteryCapacity">Dung lượng pin (kWh)</label>
                  <input
                    type="number"
                    id="batteryCapacity"
                    name="batteryCapacity"
                    value={formData.batteryCapacity}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="0.1"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="range">Tầm hoạt động (km)</label>
                  <input
                    type="number"
                    id="range"
                    name="range"
                    value={formData.range}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="chargingTime">Thời gian sạc (giờ)</label>
                  <input
                    type="number"
                    id="chargingTime"
                    name="chargingTime"
                    value={formData.chargingTime}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="0.1"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="maxSpeed">Tốc độ tối đa (km/h)</label>
                  <input
                    type="number"
                    id="maxSpeed"
                    name="maxSpeed"
                    value={formData.maxSpeed}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="acceleration">Gia tốc 0-100km/h (giây)</label>
                  <input
                    type="number"
                    id="acceleration"
                    name="acceleration"
                    value={formData.acceleration}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="0.1"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="price">Giá (VNĐ) *</label>
                  <input
                    type="number"
                    id="price"
                    name="price"
                    value={formData.price}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="1000000"
                  />
                </div>
              </>
            )}

            {vehicleType === 'color' && (
              <>
                <div className="form-group">
                  <label htmlFor="colorName">Tên màu sắc *</label>
                  <input
                    type="text"
                    id="colorName"
                    name="colorName"
                    value={formData.colorName}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tên màu sắc"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="colorCode">Mã màu sắc *</label>
                  <input
                    type="text"
                    id="colorCode"
                    name="colorCode"
                    value={formData.colorCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã màu sắc"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="hexCode">Mã màu HEX</label>
                  <input
                    type="color"
                    id="hexCode"
                    name="hexCode"
                    value={formData.hexCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                  />
                </div>

                <div className="form-group">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      name="isMetallic"
                      checked={formData.isMetallic}
                      onChange={handleInputChange}
                      disabled={mode === 'view'}
                    />
                    <span>Màu kim loại</span>
                  </label>
                </div>
              </>
            )}

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                />
                <span>Đang hoạt động</span>
              </label>
            </div>

            {(vehicleType === 'brand' || vehicleType === 'model') && (
              <div className="form-group full-width">
                <label htmlFor="description">Mô tả</label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  disabled={mode === 'view'}
                  className="form-textarea"
                  placeholder="Nhập mô tả"
                  rows="3"
                />
              </div>
            )}
          </div>

          {mode === 'view' && vehicle && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {vehicle.createdAt ? new Date(vehicle.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
              </div>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {(mode === 'edit' || mode === 'create') && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo mới' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default VehicleModal;
