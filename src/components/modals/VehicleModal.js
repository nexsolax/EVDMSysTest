import React, { useState, useEffect } from 'react';
import { X, Save, Car, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const VehicleModal = ({ vehicle, isOpen, onClose, onSave, mode = 'view', vehicleType = 'brand' }) => {
  const [formData, setFormData] = useState({
    // Brand fields (following guide field names)
    brandName: '',
    country: '',
    foundedYear: '',
    isActive: true,
    
    // Model fields (following guide field names)
    modelName: '',
    brandId: '',
    modelYear: new Date().getFullYear(),
    vehicleType: 'SEDAN',
    description: '',
    
    // Variant fields (following guide field names)
    variantName: '',
    modelId: '',
    batteryCapacity: 0,
    rangeKm: 0,
    chargingTimeFast: 0,
    chargingTimeSlow: 0,
    topSpeed: 0,
    acceleration0100: 0,
    priceBase: 0,
    
    // Color fields
    colorName: '',
    colorCode: ''
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
          country: '',
          foundedYear: '',
          isActive: true,
          modelName: '',
          brandId: '',
          modelYear: new Date().getFullYear(),
          vehicleType: 'SEDAN',
          description: '',
          variantName: '',
          modelId: '',
          batteryCapacity: 0,
          rangeKm: 0,
          chargingTimeFast: 0,
          chargingTimeSlow: 0,
          topSpeed: 0,
          acceleration0100: 0,
          priceBase: 0,
          colorName: '',
          colorCode: ''
        });
      } else if (vehicle) {
        setFormData({
          brandName: vehicle.brandName || '',
          country: vehicle.country || '',
          foundedYear: vehicle.foundedYear || '',
          isActive: vehicle.isActive !== undefined ? vehicle.isActive : true,
          modelName: vehicle.modelName || '',
          brandId: vehicle.brand?.brandId || vehicle.brandId || '',
          modelYear: vehicle.modelYear || vehicle.year || new Date().getFullYear(),
          vehicleType: vehicle.vehicleType || vehicle.bodyType || 'SEDAN',
          description: vehicle.description || '',
          variantName: vehicle.variantName || '',
          modelId: vehicle.model?.modelId || vehicle.modelId || '',
          batteryCapacity: vehicle.batteryCapacity || 0,
          rangeKm: vehicle.rangeKm || vehicle.range || 0,
          chargingTimeFast: vehicle.chargingTimeFast || 0,
          chargingTimeSlow: vehicle.chargingTimeSlow || 0,
          topSpeed: vehicle.topSpeed || vehicle.maxSpeed || 0,
          acceleration0100: vehicle.acceleration0100 || vehicle.acceleration || 0,
          priceBase: vehicle.priceBase || vehicle.price || 0,
          colorName: vehicle.colorName || '',
          colorCode: vehicle.colorCode || ''
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
    } else if (vehicleType === 'model') {
      if (!formData.modelName.trim()) {
        toast.error('Tên dòng xe là bắt buộc');
        return;
      }
      if (!formData.modelYear) {
        toast.error('Năm sản xuất là bắt buộc');
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
      if (!formData.modelId) {
        toast.error('Vui lòng chọn dòng xe');
        return;
      }
      if (formData.priceBase <= 0) {
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
          isActive: formData.isActive !== undefined ? formData.isActive : true
        };
        // Optional fields - chỉ thêm nếu có giá trị
        if (formData.country?.trim()) {
          submitData.country = formData.country.trim();
        }
        // foundedYear không có trong VehicleBrandRequest - không gửi
      } else if (vehicleType === 'model') {
        submitData = {
          modelName: formData.modelName.trim(),
          brandId: formData.brandId,
          isActive: formData.isActive !== undefined ? formData.isActive : true
        };
        // Optional fields - chỉ thêm nếu có giá trị
        if (formData.modelYear) {
          submitData.modelYear = parseInt(formData.modelYear, 10);
        }
        if (formData.vehicleType?.trim()) {
          submitData.vehicleType = formData.vehicleType.trim();
        }
        if (formData.description?.trim()) {
          submitData.description = formData.description.trim();
        }
      } else if (vehicleType === 'variant') {
        submitData = {
          variantName: formData.variantName.trim(),
          modelId: formData.modelId,
          priceBase: parseFloat(formData.priceBase),
          isActive: formData.isActive !== undefined ? formData.isActive : true
        };
        // Optional fields - chỉ thêm nếu có giá trị
        if (formData.batteryCapacity) {
          submitData.batteryCapacity = parseFloat(formData.batteryCapacity);
        }
        if (formData.rangeKm) {
          submitData.rangeKm = parseInt(formData.rangeKm, 10);
        }
        if (formData.powerKw) {
          submitData.powerKw = parseInt(formData.powerKw, 10); // Integer theo FIELD_REFERENCE_GUIDE.md line 110
        }
        if (formData.acceleration0100) {
          submitData.acceleration0100 = parseFloat(formData.acceleration0100);
        }
        if (formData.topSpeed) {
          submitData.topSpeed = parseInt(formData.topSpeed, 10);
        }
        if (formData.chargingTimeFast) {
          submitData.chargingTimeFast = parseInt(formData.chargingTimeFast, 10);
        }
        if (formData.chargingTimeSlow) {
          submitData.chargingTimeSlow = parseInt(formData.chargingTimeSlow, 10);
        }
      } else if (vehicleType === 'color') {
        submitData = {
          colorName: formData.colorName.trim(),
          colorCode: formData.colorCode.trim(),
          isActive: formData.isActive !== undefined ? formData.isActive : true
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

                <div className="form-group">
                  <label htmlFor="foundedYear">Năm thành lập</label>
                  <input
                    type="number"
                    id="foundedYear"
                    name="foundedYear"
                    value={formData.foundedYear}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="1800"
                    max={new Date().getFullYear()}
                    placeholder="Ví dụ: 2003"
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
                  <label htmlFor="modelYear">Năm sản xuất *</label>
                  <input
                    type="number"
                    id="modelYear"
                    name="modelYear"
                    value={formData.modelYear}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="1900"
                    max="2030"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="vehicleType">Loại xe</label>
                  <select
                    id="vehicleType"
                    name="vehicleType"
                    value={formData.vehicleType}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="">-- Chọn loại xe --</option>
                    <option value="SEDAN">Sedan</option>
                    <option value="SUV">SUV</option>
                    <option value="HATCHBACK">Hatchback</option>
                    <option value="COUPE">Coupe</option>
                    <option value="TRUCK">Truck</option>
                    <option value="MPV">MPV</option>
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
                  <label htmlFor="rangeKm">Tầm hoạt động (km)</label>
                  <input
                    type="number"
                    id="rangeKm"
                    name="rangeKm"
                    value={formData.rangeKm}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="powerKw">Công suất động cơ (kW)</label>
                  <input
                    type="number"
                    id="powerKw"
                    name="powerKw"
                    value={formData.powerKw || ''}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="0.1"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="chargingTimeFast">Thời gian sạc nhanh (phút)</label>
                  <input
                    type="number"
                    id="chargingTimeFast"
                    name="chargingTimeFast"
                    value={formData.chargingTimeFast}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="chargingTimeSlow">Thời gian sạc chậm (phút)</label>
                  <input
                    type="number"
                    id="chargingTimeSlow"
                    name="chargingTimeSlow"
                    value={formData.chargingTimeSlow}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="topSpeed">Tốc độ tối đa (km/h)</label>
                  <input
                    type="number"
                    id="topSpeed"
                    name="topSpeed"
                    value={formData.topSpeed}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="acceleration0100">Gia tốc 0-100km/h (giây)</label>
                  <input
                    type="number"
                    id="acceleration0100"
                    name="acceleration0100"
                    value={formData.acceleration0100}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    min="0"
                    step="0.1"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="priceBase">Giá cơ bản (VNĐ) *</label>
                  <input
                    type="number"
                    id="priceBase"
                    name="priceBase"
                    value={formData.priceBase}
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

            {vehicleType === 'model' && (
              <div className="form-group full-width">
                <label htmlFor="description">Mô tả</label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description || ''}
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
