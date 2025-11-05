import React, { useEffect, useState } from 'react';
import { X, Save, Package, Hash } from 'lucide-react';
import { inventoryAPI, warehouseAPI, vehicleAPI } from '../../services/api';
import VehicleImage from '../VehicleImage';
import toast from 'react-hot-toast';
import './Modal.css';

const InventoryItemModal = ({ item, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    variantId: '',
    warehouseId: '',
    colorId: '',
    vin: '',
    chassisNumber: '',
    manufacturingDate: '',
    purchasePrice: '',
    sellingPrice: '',
    location: '',
    status: 'available'
  });
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [colors, setColors] = useState([]);
  const [vinError, setVinError] = useState('');
  const [checkingVin, setCheckingVin] = useState(false);
  const [inventoryItemData, setInventoryItemData] = useState(null); // Store full inventory item data for image display

  useEffect(() => {
    if (!isOpen) return;
    const init = async () => {
      // Load all dropdown data first - MUST complete before loading item details
      const [loadedWarehouses] = await Promise.all([
        loadWarehouses(), 
        loadVehicles(), 
        loadColors()
      ]);
      
      // Use loaded warehouses directly instead of waiting for state update
      if (mode === 'create') {
        setFormData({
          variantId: '',
          warehouseId: '',
          colorId: '',
          vin: '',
          chassisNumber: '',
          manufacturingDate: '',
          purchasePrice: '',
          sellingPrice: '',
          location: '',
          status: 'available'
        });
        setVinError(''); // Clear VIN error when creating new
      } else if (item) {
        // Always load details for view and edit modes to ensure we have full data
        if (mode === 'view' || mode === 'edit') {
          // Pass loaded warehouses to loadItemDetails
          await loadItemDetails(loadedWarehouses);
        } else {
          // Convert IDs to strings for consistent comparison
          setFormData({
            variantId: String(item.variant?.variantId || item.variantId || ''),
            warehouseId: String(item.warehouse?.warehouseId || item.warehouseId || ''),
            colorId: String(item.color?.colorId || item.colorId || ''),
            vin: item.vin || '',
            chassisNumber: item.chassisNumber || '',
            manufacturingDate: item.manufacturingDate ? item.manufacturingDate.split('T')[0] : '',
            purchasePrice: item.costPrice || item.purchasePrice || '',
            sellingPrice: item.sellingPrice || '',
            location: item.warehouseLocation || item.location || '',
            status: item.status || 'available'
          });
        }
      }
    };
    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, item, mode]);

  // Update formData when warehouses are loaded (to ensure dropdown selection works)
  useEffect(() => {
    if (warehouses.length > 0 && formData.warehouseId && mode === 'view') {
      const warehouseIdStr = String(formData.warehouseId);
      const warehouseExists = warehouses.some(w => String(w.warehouseId) === warehouseIdStr);
      if (!warehouseExists) {
        console.warn('Warehouse ID not found in loaded warehouses:', {
          formDataWarehouseId: warehouseIdStr,
          availableIds: warehouses.map(w => String(w.warehouseId))
        });
      }
    }
  }, [warehouses, formData.warehouseId, mode]);

  const loadWarehouses = async () => {
    try {
      // Try getActiveWarehouses first, fallback to getWarehouses
      let res;
      if (warehouseAPI.getActiveWarehouses) {
        try {
          res = await warehouseAPI.getActiveWarehouses();
        } catch (e) {
          console.log('getActiveWarehouses failed, using getWarehouses');
          res = await warehouseAPI.getWarehouses();
        }
      } else {
        res = await warehouseAPI.getWarehouses();
      }
      const warehousesData = res?.data || [];
      console.log('Loaded warehouses:', warehousesData.length, 'items');
      console.log('Warehouses data:', warehousesData.map(w => ({ id: w.warehouseId, name: w.warehouseName })));
      
      if (warehousesData.length === 0) {
        console.warn('No warehouses returned from API!');
      }
      
      setWarehouses(warehousesData);
      return warehousesData; // Return for use in loadItemDetails
    } catch (error) {
      console.error('Error loading warehouses:', error);
      // Fallback: try getWarehouses if getActiveWarehouses fails
      try {
        const fallbackRes = await warehouseAPI.getWarehouses();
        const warehousesData = fallbackRes?.data || [];
        setWarehouses(warehousesData);
        return warehousesData;
      } catch (fallbackError) {
        console.error('Error loading warehouses (fallback):', fallbackError);
        toast.error('Không thể tải danh sách kho');
        setWarehouses([]);
        return [];
      }
    }
  };

  const loadVehicles = async () => {
    try {
      // Load variants instead of vehicles
      const res = await vehicleAPI.getVariants();
      setVehicles(res?.data || []);
    } catch (error) {
      console.error('Error loading variants:', error);
    }
  };

  const loadColors = async () => {
    try {
      const res = await vehicleAPI.getColors();
      setColors(res?.data || []);
    } catch (error) {
      console.error('Error loading colors:', error);
    }
  };

  const loadItemDetails = async (preloadedWarehouses = null) => {
    try {
      setLoading(true);
      const res = await inventoryAPI.getInventoryById(item.inventoryId || item.id);
      let i = res.data;
      
      console.log('Loaded inventory item detail:', {
        inventoryId: i?.inventoryId,
        vin: i?.vin,
        hasVariant: !!i?.variant,
        variantId: i?.variantId || i?.variant?.variantId,
        hasColor: !!i?.color,
        colorId: i?.colorId || i?.color?.colorId,
        hasWarehouse: !!i?.warehouse,
        warehouseId: i?.warehouseId || i?.warehouse?.warehouseId
      });
      
      // Extract IDs from relationship objects if available (fallback)
      if (!i.variantId && i.variant?.variantId) {
        i.variantId = i.variant.variantId;
      }
      if (!i.colorId && i.color?.colorId) {
        i.colorId = i.color.colorId;
      }
      if (!i.warehouseId && i.warehouse?.warehouseId) {
        i.warehouseId = i.warehouse.warehouseId;
      }
      
      // Enrich variant data if missing
      if (i && !i.variant && i.variantId) {
        try {
          const variantRes = await vehicleAPI.getVariant(i.variantId);
          if (variantRes?.data) {
            i.variant = variantRes.data;
            console.log('✓ Enriched variant for modal:', i.variant.variantName);
          }
        } catch (err) {
          console.warn('Could not enrich variant data:', err);
        }
      }
      
      // Enrich color data if missing
      if (i && !i.color && i.colorId) {
        try {
          const colorRes = await vehicleAPI.getColor(i.colorId);
          if (colorRes?.data) {
            i.color = colorRes.data;
            console.log('✓ Enriched color for modal:', i.color.colorName);
          }
        } catch (err) {
          console.warn('Could not enrich color data:', err);
        }
      }
      
      // Enrich warehouse data if missing
      // Backend now returns warehouseId directly in DTO (after backend fix)
      // Priority: direct field first (i.warehouseId), then nested object for backward compatibility
      const warehouseIdValue = 
        i?.warehouseId ||           // Direct field from DTO (preferred - matches backend fix)
        i?.warehouse?.warehouseId || // Nested object (fallback for backward compatibility)
        null;
      
      if (i && !i.warehouse && warehouseIdValue) {
        try {
          const warehouseRes = await warehouseAPI.getWarehouse(warehouseIdValue);
          if (warehouseRes?.data) {
            i.warehouse = warehouseRes.data;
            console.log('✓ Enriched warehouse for modal:', i.warehouse.warehouseName);
          }
        } catch (err) {
          console.warn('Could not enrich warehouse data:', err);
        }
      }
      
      console.log('Final inventory item (modal):', {
        inventoryId: i?.inventoryId,
        hasVariant: !!i?.variant,
        variantName: i?.variant?.variantName || 'N/A',
        hasColor: !!i?.color,
        colorName: i?.color?.colorName || 'N/A',
        hasWarehouse: !!i?.warehouse,
        warehouseName: i?.warehouse?.warehouseName || 'N/A'
      });
      
      // Store full inventory item data for image display
      setInventoryItemData(i);
      
      // Use preloaded warehouses if provided, otherwise use state
      const currentWarehouses = preloadedWarehouses || (warehouses.length > 0 ? warehouses : []);
      
      const warehouseIdStr = warehouseIdValue ? String(warehouseIdValue) : '';
      
      setFormData({
        variantId: String(i.variant?.variantId || i.variantId || ''),
        warehouseId: warehouseIdStr,
        colorId: String(i.color?.colorId || i.colorId || ''),
        vin: i.vin || '',
        chassisNumber: i.chassisNumber || '',
        manufacturingDate: i.manufacturingDate ? i.manufacturingDate.split('T')[0] : '',
        purchasePrice: i.costPrice || i.purchasePrice || '',
        sellingPrice: i.sellingPrice || '',
        location: i.warehouseLocation || i.location || '',
        status: i.status || 'available'
      });
    } catch (e) {
      console.error('Error loading inventory item:', e);
      toast.error('Không thể tải thông tin tồn kho');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    
    // Clear VIN error when user types
    if (name === 'vin') {
      setVinError('');
    }
  };

  // Check if VIN already exists
  const checkVinExists = async (vin) => {
    if (!vin || vin.trim() === '') {
      setVinError('');
      return false;
    }

    try {
      setCheckingVin(true);
      const response = await inventoryAPI.getInventoryByVin(vin.trim());
      
      // If we get a response, VIN exists
      if (response.data) {
        const existingInventory = response.data;
        
        // In edit mode, if the VIN belongs to the current item being edited, it's OK
        if (mode === 'edit' && item?.inventoryId) {
          if (existingInventory.inventoryId === item.inventoryId) {
            setVinError('');
            return false; // VIN belongs to current item, not a duplicate
          }
        }
        
        // VIN already exists for another inventory
        setVinError('Số VIN này đã tồn tại trong hệ thống');
        return true;
      }
      
      setVinError('');
      return false; // VIN doesn't exist
    } catch (error) {
      // If 404, VIN doesn't exist (this is OK)
      if (error.response?.status === 404) {
        setVinError('');
        return false;
      }
      
      // Other errors - don't block submission, just log
      console.error('Error checking VIN:', error);
      setVinError('');
      return false;
    } finally {
      setCheckingVin(false);
    }
  };

  // Handle VIN blur event to check for duplicates
  const handleVinBlur = async (e) => {
    const vin = e.target.value.trim();
    if (vin) {
      await checkVinExists(vin);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;
    
    // Validate VIN before submitting
    const vin = formData.vin.trim();
    if (!vin) {
      toast.error('Vui lòng nhập số VIN');
      return;
    }

    // Check if VIN is duplicate
    const isDuplicate = await checkVinExists(vin);
    if (isDuplicate) {
      toast.error('Số VIN này đã tồn tại. Vui lòng nhập số VIN khác.');
      return;
    }

    try {
      setLoading(true);
      
      // Prepare submit data according to VehicleInventoryRequest DTO (API_WAREHOUSE_INVENTORY_GUIDE.md)
      // Field types: variantId (Integer), colorId (Integer), warehouseId (UUID/String), vin (String)
      // Field mappings: purchasePrice → costPrice, location → warehouseLocation
      const submitData = {
        variantId: formData.variantId ? parseInt(formData.variantId, 10) : null,
        colorId: formData.colorId ? parseInt(formData.colorId, 10) : null,
        warehouseId: formData.warehouseId && formData.warehouseId.toString().trim() !== '' 
          ? formData.warehouseId.toString().trim() // UUID as string, not integer
          : null,
        vin: vin, // Use trimmed VIN (required, max 17 chars, unique)
        chassisNumber: formData.chassisNumber?.trim() || null, // Optional, max 50 chars
        manufacturingDate: formData.manufacturingDate || null, // Optional, format: yyyy-MM-dd
        purchasePrice: formData.purchasePrice ? parseFloat(formData.purchasePrice) : null, // Optional, BigDecimal - maps to costPrice
        sellingPrice: formData.sellingPrice ? parseFloat(formData.sellingPrice) : null, // Optional, BigDecimal
        location: formData.location?.trim() || null, // Optional, maps to warehouseLocation
        status: formData.status || 'available' // Optional, default: "available"
      };
      
      // Log for debugging (can be removed in production)
      if (process.env.NODE_ENV === 'development') {
        console.log('Submitting inventory data:', submitData);
      }
      await onSave(mode === 'create' ? null : item?.inventoryId || item?.id, submitData);
      onClose();
    } catch (e) {
      // upstream toast
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <Package size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết tồn kho' : 
               mode === 'create' ? 'Thêm tồn kho mới' : 
               'Chỉnh sửa tồn kho'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {/* Vehicle Image Display */}
          {(inventoryItemData || item) && (
            <div style={{ 
              marginBottom: '20px', 
              padding: '16px', 
              backgroundColor: '#f9fafb', 
              borderRadius: '8px',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              minHeight: '200px'
            }}>
              <VehicleImage 
                vehicle={inventoryItemData || item} 
                className="inventory-vehicle-image"
                size={48}
              />
            </div>
          )}

          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="variantId">Phiên bản xe</label>
              <select id="variantId" name="variantId" className="form-select" value={formData.variantId} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="">Chọn phiên bản xe</option>
                {vehicles.map((variant) => (
                  <option key={variant.variantId} value={variant.variantId}>
                    {variant.model?.brand?.brandName} {variant.model?.modelName} {variant.variantName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="warehouseId">Kho</label>
              <select id="warehouseId" name="warehouseId" className="form-select" value={String(formData.warehouseId || '')} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="">Chọn kho</option>
                {warehouses.map((w) => {
                  const whId = String(w.warehouseId || '');
                  return (
                    <option key={whId} value={whId}>
                      {w.warehouseName}
                    </option>
                  );
                })}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="colorId">Màu</label>
              <select id="colorId" name="colorId" className="form-select" value={formData.colorId} onChange={handleChange} disabled={mode === 'view'}>
                <option value="">Chọn màu</option>
                {colors.map((color) => (
                  <option key={color.colorId} value={color.colorId}>
                    {color.colorName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="vin">VIN</label>
              <div className="input-with-icon">
                <Hash size={16} />
                <input 
                  id="vin" 
                  name="vin" 
                  className={`form-input ${vinError ? 'input-error' : ''}`} 
                  value={formData.vin} 
                  onChange={handleChange}
                  onBlur={handleVinBlur}
                  disabled={mode === 'view' || checkingVin} 
                  required 
                  placeholder="Nhập số VIN"
                />
                {checkingVin && (
                  <span style={{ fontSize: '12px', color: '#666', marginLeft: '8px' }}>Đang kiểm tra...</span>
                )}
              </div>
              {vinError && (
                <div style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px' }}>
                  {vinError}
                </div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="chassisNumber">Số khung (tùy chọn)</label>
              <input
                id="chassisNumber"
                name="chassisNumber"
                className="form-input"
                value={formData.chassisNumber}
                onChange={handleChange}
                disabled={mode === 'view'}
                placeholder="Nhập số khung"
                maxLength={50}
              />
            </div>

            <div className="form-group">
              <label htmlFor="manufacturingDate">Ngày sản xuất (tùy chọn)</label>
              <input
                type="date"
                id="manufacturingDate"
                name="manufacturingDate"
                className="form-input"
                value={formData.manufacturingDate}
                onChange={handleChange}
                disabled={mode === 'view'}
              />
            </div>

            <div className="form-group">
              <label htmlFor="purchasePrice">Giá mua (VNĐ) - tùy chọn</label>
              <input
                type="number"
                id="purchasePrice"
                name="purchasePrice"
                className="form-input"
                value={formData.purchasePrice}
                onChange={handleChange}
                disabled={mode === 'view'}
                min="0"
                step="1000000"
                placeholder="Giá mua vào"
              />
              <small className="form-help">Sẽ được lưu thành costPrice trong hệ thống</small>
            </div>

            <div className="form-group">
              <label htmlFor="sellingPrice">Giá bán (VNĐ) - tùy chọn</label>
              <input
                type="number"
                id="sellingPrice"
                name="sellingPrice"
                className="form-input"
                value={formData.sellingPrice}
                onChange={handleChange}
                disabled={mode === 'view'}
                min="0"
                step="1000000"
                placeholder="Giá bán ra"
              />
            </div>

            <div className="form-group">
              <label htmlFor="location">Vị trí trong kho (tùy chọn)</label>
              <input
                type="text"
                id="location"
                name="location"
                className="form-input"
                value={formData.location}
                onChange={handleChange}
                disabled={mode === 'view'}
                placeholder="Ví dụ: Khu A, Tầng 2, Vị trí 15"
              />
              <small className="form-help">Sẽ được lưu thành warehouseLocation trong hệ thống</small>
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select id="status" name="status" className="form-select" value={formData.status} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="available">Có sẵn</option>
                <option value="reserved">Đã đặt</option>
                <option value="sold">Đã bán</option>
                <option value="maintenance">Bảo trì</option>
                <option value="damaged">Hư hỏng</option>
                <option value="in_transit">Đang vận chuyển</option>
                <option value="pending_delivery">Chờ giao hàng</option>
              </select>
            </div>

          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {(mode === 'edit' || mode === 'create') && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo tồn kho' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default InventoryItemModal;
