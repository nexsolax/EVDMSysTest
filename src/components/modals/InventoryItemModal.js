import React, { useEffect, useState } from 'react';
import { X, Save, Package, Hash } from 'lucide-react';
import { inventoryAPI, warehouseAPI, vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const InventoryItemModal = ({ item, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    variantId: '',
    warehouseId: '',
    colorId: '',
    vin: '',
    status: 'available',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [colors, setColors] = useState([]);

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
          status: 'available',
          notes: ''
        });
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
            status: item.status || 'available',
            notes: item.notes || ''
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
      const i = res.data;
      
      // Use preloaded warehouses if provided, otherwise use state
      const currentWarehouses = preloadedWarehouses || (warehouses.length > 0 ? warehouses : []);
      
      // Backend now returns warehouseId directly in DTO (after backend fix)
      // Priority: direct field first (i.warehouseId), then nested object for backward compatibility
      const warehouseIdValue = 
        i.warehouseId ||           // Direct field from DTO (preferred - matches backend fix)
        i.warehouse?.warehouseId || // Nested object (fallback for backward compatibility)
        null;
      
      const warehouseIdStr = warehouseIdValue ? String(warehouseIdValue) : '';
      
      setFormData({
        variantId: String(i.variant?.variantId || i.variantId || ''),
        warehouseId: warehouseIdStr,
        colorId: String(i.color?.colorId || i.colorId || ''),
        vin: i.vin || '',
        status: i.status || 'available',
        notes: i.notes || ''
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
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;
    try {
      setLoading(true);
      
      // Prepare submit data - ensure warehouseId is UUID string or null (not empty string)
      const submitData = {
        ...formData,
        warehouseId: formData.warehouseId && formData.warehouseId.trim() !== '' 
          ? formData.warehouseId 
          : null, // Send null instead of empty string for optional field
        variantId: formData.variantId || null,
        colorId: formData.colorId && formData.colorId.trim() !== '' 
          ? formData.colorId 
          : null, // Optional field
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
                <input id="vin" name="vin" className="form-input" value={formData.vin} onChange={handleChange} disabled={mode === 'view'} required />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="status">Trạng thái</label>
              <select id="status" name="status" className="form-select" value={formData.status} onChange={handleChange} disabled={mode === 'view'} required>
                <option value="available">Có sẵn</option>
                <option value="reserved">Đã đặt</option>
                <option value="sold">Đã bán</option>
                <option value="in_stock">Trong kho</option>
                <option value="transit">Đang vận chuyển</option>
                <option value="maintenance">Bảo trì</option>
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="notes">Ghi chú</label>
              <textarea id="notes" name="notes" className="form-input" rows={3} value={formData.notes} onChange={handleChange} disabled={mode === 'view'} placeholder="Ghi chú về tồn kho..." />
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
