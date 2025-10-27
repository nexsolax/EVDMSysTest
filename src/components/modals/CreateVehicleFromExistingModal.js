import React, { useState, useEffect } from 'react';
import { X, Upload, AlertCircle, CheckCircle, Car, Zap, Palette, Package, Image as ImageIcon, ChevronDown, Plus, Building } from 'lucide-react';
import { vehicleAPI, imageAPI, warehouseAPI, inventoryAPI } from '../../services/api';
import './Modal.css';

const CreateVehicleFromExistingModal = ({ isOpen, onClose, onSave }) => {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [creationMode, setCreationMode] = useState('existing'); // Only 'existing' mode
  
  // Data for dropdowns
  const [brands, setBrands] = useState([]);
  const [models, setModels] = useState([]);
  const [colors, setColors] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseLocations, setWarehouseLocations] = useState([]);
  
  // Form data
  const [formData, setFormData] = useState({
    // Required fields for existing data
    existingBrandId: '',
    existingModelId: '',
    existingColorId: '',
    existingWarehouseId: '',
    variant: {
      variantName: '',
      batteryCapacity: '',
      rangeKm: '',
      powerKw: '',
      acceleration0100: '',
      topSpeed: '',
      chargingTimeFast: '',
      chargingTimeSlow: '',
      priceBase: ''
    },
    inventory: {
      warehouseLocation: '',
      vin: '',
      chassisNumber: '',
      status: 'available',
      sellingPrice: ''
    }
  });

  useEffect(() => {
    if (isOpen) {
      loadData();
      resetForm();
    }
  }, [isOpen]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      
      console.log('Loading brands, colors, and warehouses...');
      
      // Use existing API endpoints that work
      const [brandsRes, colorsRes, warehousesRes] = await Promise.all([
        vehicleAPI.getBrands(),
        vehicleAPI.getColors(),
        warehouseAPI.getWarehouses()
      ]);
      
      console.log('Brands response:', brandsRes.data);
      console.log('Colors response:', colorsRes.data);
      console.log('Warehouses response:', warehousesRes.data);
      
      setBrands(brandsRes.data || []);
      setColors(colorsRes.data || []);
      setWarehouses(warehousesRes.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
      setError('Không thể tải dữ liệu. Vui lòng kiểm tra kết nối và thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const loadModelsByBrand = async (brandId) => {
    if (!brandId) {
      setModels([]);
      return;
    }

    try {
      console.log('Loading models for brand ID:', brandId);

      // Try multiple API endpoints
      let models = [];

      try {
        // Try public API first (most reliable)
        const response = await fetch(`/api/public/vehicle-models/brand/${brandId}`);
        if (response.ok) {
          models = await response.json();
          console.log('Public API response:', models);
        }
      } catch (publicError) {
        console.warn('Public API failed, trying authenticated API:', publicError);

        try {
          // Try authenticated API
          const response = await vehicleAPI.getModels();
          const allModels = response.data || [];
          models = allModels.filter(model => model.brandId === parseInt(brandId));
          console.log('Authenticated API response:', models);
        } catch (authError) {
          console.warn('Authenticated API failed, trying direct API:', authError);

          try {
            // Try direct API call
            const response = await fetch(`/api/vehicles/models/brand/${brandId}`);
            if (response.ok) {
              models = await response.json();
              console.log('Direct API response:', models);
            }
          } catch (directError) {
            console.error('All API endpoints failed:', directError);
          }
        }
      }

      setModels(models);
    } catch (error) {
      console.error('Error loading models by brand:', error);
      setModels([]);
    }
  };

  const loadWarehouseLocations = async (warehouseId) => {
    if (!warehouseId) {
      setWarehouseLocations([]);
      return;
    }
    
    try {
      console.log('Loading locations for warehouse ID:', warehouseId);
      
      // Try multiple approaches to get warehouse locations
      let locations = [];
      
      try {
        // Try to get warehouse info first
        const warehouseResponse = await warehouseAPI.getWarehouse(warehouseId);
        const warehouse = warehouseResponse.data;
        console.log('Warehouse info:', warehouse);
        
        // Try to get existing inventory for this warehouse
        const inventoryResponse = await inventoryAPI.getInventory();
        const inventory = inventoryResponse.data || [];
        
        // Filter by warehouse and extract unique locations
        const warehouseInventory = inventory.filter(item => item.warehouseId === warehouseId);
        const uniqueLocations = [...new Set(warehouseInventory.map(item => item.warehouseLocation).filter(loc => loc))];
        
        if (uniqueLocations.length > 0) {
          locations = uniqueLocations.map(location => ({ locationId: location, locationName: location }));
          console.log('Found existing locations from inventory:', locations);
        } else {
          // Generate default locations based on warehouse info
          const warehouseCode = warehouse?.warehouseCode || 'MAIN';
          locations = [
            { locationId: `${warehouseCode}-A-01`, locationName: `${warehouseCode}-A-01` },
            { locationId: `${warehouseCode}-A-02`, locationName: `${warehouseCode}-A-02` },
            { locationId: `${warehouseCode}-A-03`, locationName: `${warehouseCode}-A-03` },
            { locationId: `${warehouseCode}-B-01`, locationName: `${warehouseCode}-B-01` },
            { locationId: `${warehouseCode}-B-02`, locationName: `${warehouseCode}-B-02` },
            { locationId: `${warehouseCode}-C-01`, locationName: `${warehouseCode}-C-01` }
          ];
          console.log('Generated default locations:', locations);
        }
      } catch (warehouseError) {
        console.warn('Warehouse API failed, using default locations:', warehouseError);
        
        // Fallback: generate some default locations
        locations = [
          { locationId: 'A-01-01', locationName: 'A-01-01' },
          { locationId: 'A-01-02', locationName: 'A-01-02' },
          { locationId: 'A-01-03', locationName: 'A-01-03' },
          { locationId: 'B-01-01', locationName: 'B-01-01' },
          { locationId: 'B-01-02', locationName: 'B-01-02' },
          { locationId: 'C-01-01', locationName: 'C-01-01' }
        ];
        console.log('Using fallback locations:', locations);
      }
      
      setWarehouseLocations(locations);
    } catch (error) {
      console.error('Error loading warehouse locations:', error);
      setWarehouseLocations([]);
    }
  };

  const resetForm = () => {
    setFormData({
      existingBrandId: '',
      existingModelId: '',
      existingColorId: '',
      existingWarehouseId: '',
      variant: {
        variantName: '',
        batteryCapacity: '',
        rangeKm: '',
        powerKw: '',
        acceleration0100: '',
        topSpeed: '',
        chargingTimeFast: '',
        chargingTimeSlow: '',
        priceBase: ''
      },
      inventory: {
        warehouseLocation: '',
        vin: '',
        chassisNumber: '',
        status: 'available',
        sellingPrice: ''
      }
    });
    setError('');
    setSuccess('');
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    // Handle nested form data
    if (name.startsWith('variant.')) {
      const fieldName = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        variant: {
          ...prev.variant,
          [fieldName]: value
        }
      }));
    } else if (name.startsWith('inventory.')) {
      const fieldName = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        inventory: {
          ...prev.inventory,
          [fieldName]: value
        }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }

    // Load models when brand changes
    if (name === 'existingBrandId') {
      console.log('Brand changed to:', value);
      loadModelsByBrand(value);
      // Reset model selection
      setFormData(prev => ({
        ...prev,
        existingBrandId: value,
        existingModelId: ''
      }));
    }
    
    // Load warehouse locations when warehouse changes
    if (name === 'existingWarehouseId') {
      console.log('Warehouse changed to:', value);
      loadWarehouseLocations(value);
      // Reset warehouse location selection
      setFormData(prev => ({
        ...prev,
        existingWarehouseId: value,
        'inventory.warehouseLocation': ''
      }));
    }
  };

  const handleFileChange = (e) => {
    const { name, files } = e.target;
    
    if (name === 'mainImage') {
      setFormData(prev => ({
        ...prev,
        images: {
          ...prev.images,
          mainImage: files[0] || null
        }
      }));
    } else if (name === 'interiorImages') {
      setFormData(prev => ({
        ...prev,
        images: {
          ...prev.images,
          interiorImages: Array.from(files)
        }
      }));
    } else if (name === 'exteriorImages') {
      setFormData(prev => ({
        ...prev,
        images: {
          ...prev.images,
          exteriorImages: Array.from(files)
        }
      }));
    } else if (name === 'variantImage') {
      setFormData(prev => ({
        ...prev,
        variant: {
          ...prev.variant,
          variantImageUrl: files[0] || null
        }
      }));
    }
  };

  const validateForm = () => {
    const requiredFields = [
      'existingBrandId', 
      'existingModelId', 
      'existingColorId', 
      'existingWarehouseId',
      'variant.variantName',
      'variant.priceBase',
      'inventory.vin'
    ];
    
    const missingFields = requiredFields.filter(field => {
      const value = field.includes('.') 
        ? formData[field.split('.')[0]][field.split('.')[1]]
        : formData[field];
      return !value;
    });
    
    if (missingFields.length > 0) {
      setError(`Vui lòng điền các trường bắt buộc: ${missingFields.join(', ')}`);
      return false;
    }

    // Validate VIN format (basic check)
    if (formData.inventory.vin && formData.inventory.vin.length < 17) {
      setError('Số VIN phải có ít nhất 17 ký tự');
      return false;
    }

    // Validate price
    if (formData.variant.priceBase && isNaN(Number(formData.variant.priceBase))) {
      setError('Giá bán cơ bản phải là số');
      return false;
    }

    // Validate selling price
    if (formData.inventory.sellingPrice && isNaN(Number(formData.inventory.sellingPrice))) {
      setError('Giá bán phải là số');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setSaving(true);
    setError('');
    setSuccess('');

    try {
      // Prepare data for existing data mode only
      const submitData = {
        existingBrandId: parseInt(formData.existingBrandId),
        existingModelId: parseInt(formData.existingModelId),
        existingColorId: parseInt(formData.existingColorId),
        existingWarehouseId: formData.existingWarehouseId,
        variant: {
          variantName: formData.variant.variantName,
          batteryCapacity: formData.variant.batteryCapacity ? parseFloat(formData.variant.batteryCapacity) : null,
          rangeKm: formData.variant.rangeKm ? parseInt(formData.variant.rangeKm) : null,
          powerKw: formData.variant.powerKw ? parseFloat(formData.variant.powerKw) : null,
          acceleration0100: formData.variant.acceleration0100 ? parseFloat(formData.variant.acceleration0100) : null,
          topSpeed: formData.variant.topSpeed ? parseInt(formData.variant.topSpeed) : null,
          chargingTimeFast: formData.variant.chargingTimeFast ? parseInt(formData.variant.chargingTimeFast) : null,
          chargingTimeSlow: formData.variant.chargingTimeSlow ? parseInt(formData.variant.chargingTimeSlow) : null,
          priceBase: formData.variant.priceBase ? parseFloat(formData.variant.priceBase) : null
        },
        inventory: {
          warehouseLocation: formData.inventory.warehouseLocation,
          vin: formData.inventory.vin,
          chassisNumber: formData.inventory.chassisNumber,
          status: formData.inventory.status,
          sellingPrice: formData.inventory.sellingPrice ? parseFloat(formData.inventory.sellingPrice) : null,
          notes: `Created via frontend - ${new Date().toISOString()}`
        }
      };

      console.log('Submitting data:', submitData);
      const response = await vehicleAPI.createVehicleFromExistingJson(submitData);
      
      console.log('API Response:', response.data);
      
      if (response.data && response.data.success) {
        setSuccess('Tạo xe thành công!');
        setTimeout(() => {
          onSave();
          onClose();
        }, 1500);
      } else {
        setError(response.data?.message || 'Có lỗi xảy ra khi tạo xe');
      }
      
    } catch (error) {
      console.error('Error creating vehicle:', error);
      
      // Better error handling
      if (error.response) {
        // Server responded with error status
        const errorMessage = error.response.data?.message || 
                           error.response.data?.error || 
                           `Lỗi server: ${error.response.status}`;
        setError(errorMessage);
      } else if (error.request) {
        // Network error
        setError('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.');
      } else {
        // Other error
        setError('Có lỗi xảy ra khi tạo xe. Vui lòng thử lại.');
      }
    } finally {
      setSaving(false);
    }
  };

  const renderFieldGroup = (title, Icon, fields) => (
    <div className="field-group">
      <div className="field-group-header">
        <Icon className="field-group-icon" />
        <h3 className="field-group-title">{title}</h3>
      </div>
      <div className="field-group-content">
        {fields.map(field => (
          <div key={field.name} className="form-group">
            <label className={`form-label ${field.required ? 'required' : ''}`}>
              {field.label}
              {field.required && <span className="required-mark">*</span>}
            </label>
            {field.type === 'file' ? (
              <input
                type="file"
                name={field.name}
                onChange={handleFileChange}
                accept={field.accept}
                multiple={field.multiple}
                className="form-input file-input"
              />
            ) : field.type === 'select' ? (
              <select
                name={field.name}
                value={field.name.includes('.') 
                  ? formData[field.name.split('.')[0]][field.name.split('.')[1]]
                  : formData[field.name]
                }
                onChange={handleInputChange}
                className="form-input"
                disabled={field.disabled}
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
                value={field.name.includes('.') 
                  ? formData[field.name.split('.')[0]][field.name.split('.')[1]]
                  : formData[field.name]
                }
                onChange={handleInputChange}
                placeholder={field.placeholder}
                className="form-input"
                rows={3}
              />
            ) : (
              <input
                type={field.type}
                name={field.name}
                value={field.name.includes('.') 
                  ? formData[field.name.split('.')[0]][field.name.split('.')[1]]
                  : formData[field.name]
                }
                onChange={handleInputChange}
                placeholder={field.placeholder}
                className="form-input"
              />
            )}
            {field.description && (
              <small className="field-description">{field.description}</small>
            )}
          </div>
        ))}
      </div>
    </div>
  );

  if (!isOpen) return null;

  // Don't render form until data is loaded
  if (loading) {
    return (
      <div className="modal-overlay">
        <div className="modal-container create-vehicle-modal">
          <div className="modal-header">
            <h2 className="modal-title">
              <Car className="modal-title-icon" />
              Tạo xe mới
            </h2>
            <button className="modal-close" onClick={onClose}>
              <X size={20} />
            </button>
          </div>
          <div className="modal-content">
            <div className="loading-state">
              <div className="loading-spinner"></div>
              <p>Đang tải dữ liệu...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay">
      <div className="modal-container create-vehicle-modal">
        <div className="modal-header">
          <h2 className="modal-title">
            <Car className="modal-title-icon" />
            Tạo xe mới
          </h2>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-content">
          <div className="field-info">
            <div className="field-info-item">
              <CheckCircle className="field-info-icon" />
              <span>8 trường bắt buộc</span>
            </div>
            <div className="field-info-item">
              <AlertCircle className="field-info-icon" />
              <span>Tối đa 10MB/file, hỗ trợ: JPG, PNG, GIF, WebP</span>
            </div>
          </div>

          {error && (
            <div className="alert alert-error">
              <AlertCircle className="alert-icon" />
              {error}
            </div>
          )}

          {success && (
            <div className="alert alert-success">
              <CheckCircle className="alert-icon" />
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit} className="create-vehicle-form">
            <div className="form-sections">
              {renderFieldGroup(
                'Chọn dữ liệu có sẵn',
                Car,
                [
                  { 
                    name: 'existingBrandId', 
                    label: 'Thương hiệu', 
                    type: 'select', 
                    required: true, 
                    options: (brands || []).filter(b => b).map(b => ({ value: b.brandId || b.id, label: b.brandName || b.name }))
                  },
                  { 
                    name: 'existingModelId', 
                    label: 'Dòng xe', 
                    type: 'select', 
                    required: true, 
                    options: (models || []).filter(m => m).map(m => ({ value: m.modelId || m.id, label: m.modelName || m.name })),
                    disabled: !formData.existingBrandId
                  },
                  { 
                    name: 'existingColorId', 
                    label: 'Màu sắc', 
                    type: 'select', 
                    required: true, 
                    options: (colors || []).filter(c => c).map(c => ({ value: c.colorId || c.id, label: c.colorName || c.name }))
                  },
                  { 
                    name: 'existingWarehouseId', 
                    label: 'Kho chứa', 
                    type: 'select', 
                    required: true, 
                    options: (warehouses || []).filter(w => w).map(w => ({ value: w.warehouseId || w.id, label: w.warehouseName || w.name }))
                  }
                ]
              )}

              {renderFieldGroup(
                'Thông tin phiên bản mới',
                Zap,
                [
                  { name: 'variant.variantName', label: 'Tên phiên bản', type: 'text', required: true, placeholder: 'VD: Performance Edition' },
                  { name: 'variant.priceBase', label: 'Giá bán cơ bản (VNĐ)', type: 'number', required: true, placeholder: '1800000000' },
                  { name: 'variant.batteryCapacity', label: 'Dung lượng pin (kWh)', type: 'number', placeholder: '82' },
                  { name: 'variant.rangeKm', label: 'Tầm hoạt động (km)', type: 'number', placeholder: '560' },
                  { name: 'variant.powerKw', label: 'Công suất (kW)', type: 'number', placeholder: '340' },
                  { name: 'variant.acceleration0100', label: 'Tăng tốc 0-100km/h (s)', type: 'number', placeholder: '3.1' },
                  { name: 'variant.topSpeed', label: 'Tốc độ tối đa (km/h)', type: 'number', placeholder: '261' },
                  { name: 'variant.chargingTimeFast', label: 'Thời gian sạc nhanh (phút)', type: 'number', placeholder: '25' },
                  { name: 'variant.chargingTimeSlow', label: 'Thời gian sạc chậm (giờ)', type: 'number', placeholder: '6' },
                  { name: 'variantImage', label: 'Hình ảnh phiên bản', type: 'file', accept: 'image/*' }
                ]
              )}

              {renderFieldGroup(
                'Thông tin kho hàng',
                Package,
                [
                  { 
                    name: 'inventory.warehouseLocation', 
                    label: 'Vị trí trong kho', 
                    type: 'select', 
                    required: true, 
                    options: (warehouseLocations || []).filter(loc => loc).map(loc => ({ 
                      value: loc.locationId || loc.id || loc, 
                      label: loc.locationName || loc.name || loc 
                    })),
                    disabled: !formData.existingWarehouseId,
                    placeholder: 'Chọn vị trí trong kho'
                  },
                  { name: 'inventory.vin', label: 'Số VIN', type: 'text', required: true, placeholder: '1HGBH41JXMN109186' },
                  { name: 'inventory.chassisNumber', label: 'Số khung', type: 'text', placeholder: 'CHASSIS123456' },
                  { name: 'inventory.sellingPrice', label: 'Giá bán (VNĐ)', type: 'number', placeholder: '1850000000' },
                  { name: 'inventory.status', label: 'Trạng thái', type: 'select', options: [
                    { value: 'available', label: 'Có sẵn' },
                    { value: 'reserved', label: 'Đã đặt' },
                    { value: 'sold', label: 'Đã bán' },
                    { value: 'maintenance', label: 'Bảo trì' },
                    { value: 'damaged', label: 'Hư hỏng' },
                    { value: 'in_transit', label: 'Đang vận chuyển' },
                    { value: 'pending_delivery', label: 'Chờ giao hàng' }
                  ]}
                ]
              )}

              {/* Image upload section removed - using JSON API without file upload */}
            </div>

            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving || loading}>
                {saving ? 'Đang tạo...' : 'Tạo xe'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateVehicleFromExistingModal;
