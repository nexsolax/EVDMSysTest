import React, { useState, useEffect } from 'react';
import { X, Save, Package, Calendar } from 'lucide-react';
import { warehouseAPI, vehicleAPI, dealerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const InventoryModal = ({ item, isOpen, onClose, onSave, mode = 'view', itemType = 'warehouse' }) => {
  const [formData, setFormData] = useState({
    // Warehouse fields
    warehouseName: '',
    warehouseCode: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    phone: '',
    email: '',
    capacity: 0,
    isActive: true,
    
    // Vehicle fields
    variantId: '',
    dealerId: '',
    quantity: 0,
    unitPrice: 0,
    totalValue: 0,
    condition: 'NEW',
    location: '',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [variants, setVariants] = useState([]);
  const [dealers, setDealers] = useState([]);

  useEffect(() => {
    if (isOpen) {
      if (itemType === 'vehicles') {
        loadVariants();
        loadDealers();
      }
      
      if (mode === 'create') {
        setFormData({
          warehouseName: '',
          warehouseCode: '',
          address: '',
          city: '',
          province: '',
          postalCode: '',
          phone: '',
          email: '',
          capacity: 0,
          isActive: true,
          variantId: '',
          dealerId: '',
          quantity: 0,
          unitPrice: 0,
          totalValue: 0,
          condition: 'NEW',
          location: '',
          notes: ''
        });
      } else if (item) {
        setFormData({
          warehouseName: item.warehouseName || '',
          warehouseCode: item.warehouseCode || '',
          address: item.address || '',
          city: item.city || '',
          province: item.province || '',
          postalCode: item.postalCode || '',
          phone: item.phone || '',
          email: item.email || '',
          capacity: item.capacity || 0,
          isActive: item.isActive !== undefined ? item.isActive : true,
          variantId: item.variant?.variantId || item.variantId || '',
          dealerId: item.dealer?.dealerId || item.dealerId || '',
          quantity: item.quantity || 0,
          unitPrice: item.unitPrice || 0,
          totalValue: item.totalValue || 0,
          condition: item.condition || 'NEW',
          location: item.location || '',
          notes: item.notes || ''
        });
      }
    }
  }, [isOpen, item, mode, itemType]);

  const loadVariants = async () => {
    try {
      const response = await vehicleAPI.getVariants();
      setVariants(response.data || []);
    } catch (error) {
      console.error('Error loading variants:', error);
    }
  };

  const loadDealers = async () => {
    try {
      const response = await dealerAPI.getDealers();
      setDealers(response.data || []);
    } catch (error) {
      console.error('Error loading dealers:', error);
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

    // Validation based on item type
    if (itemType === 'warehouses') {
      if (!formData.warehouseName.trim()) {
        toast.error('Tên kho là bắt buộc');
        return;
      }
      if (!formData.warehouseCode.trim()) {
        toast.error('Mã kho là bắt buộc');
        return;
      }
      if (formData.capacity <= 0) {
        toast.error('Sức chứa phải lớn hơn 0');
        return;
      }
    } else if (itemType === 'vehicles') {
      if (!formData.variantId) {
        toast.error('Vui lòng chọn dòng xe');
        return;
      }
      if (!formData.dealerId) {
        toast.error('Vui lòng chọn đại lý');
        return;
      }
      if (formData.quantity <= 0) {
        toast.error('Số lượng phải lớn hơn 0');
        return;
      }
      if (formData.unitPrice <= 0) {
        toast.error('Đơn giá phải lớn hơn 0');
        return;
      }
    }

    try {
      setLoading(true);
      
      let submitData = {};
      
      if (itemType === 'warehouses') {
        submitData = {
          warehouseName: formData.warehouseName.trim(),
          warehouseCode: formData.warehouseCode.trim(),
          address: formData.address.trim(),
          city: formData.city.trim(),
          province: formData.province.trim(),
          postalCode: formData.postalCode.trim(),
          phone: formData.phone.trim(),
          email: formData.email.trim(),
          capacity: parseInt(formData.capacity),
          isActive: formData.isActive
        };
      } else if (itemType === 'vehicles') {
        submitData = {
          variantId: formData.variantId,
          dealerId: formData.dealerId,
          quantity: parseInt(formData.quantity),
          unitPrice: parseFloat(formData.unitPrice),
          totalValue: parseFloat(formData.unitPrice) * parseInt(formData.quantity),
          condition: formData.condition,
          location: formData.location.trim(),
          notes: formData.notes.trim()
        };
      }

      console.log('Sending inventory data:', submitData);
      
      if (mode === 'create') {
        await onSave(null, submitData);
      } else {
        await onSave(item[`${itemType === 'warehouses' ? 'warehouse' : 'vehicle'}Id`], submitData);
      }
    } catch (error) {
      console.error('Error saving inventory:', error);
      
      if (error.response) {
        const errorMessage = error.response.data?.message || error.message;
        toast.error(`Lỗi: ${errorMessage}`);
      } else {
        toast.error(mode === 'create' ? 'Không thể tạo dữ liệu kho' : 'Không thể cập nhật dữ liệu kho');
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const getTitle = () => {
    const typeNames = {
      warehouses: 'kho',
      vehicles: 'xe'
    };
    const typeName = typeNames[itemType] || 'kho';
    
    if (mode === 'view') return `Xem chi tiết ${typeName}`;
    if (mode === 'create') return `Thêm ${typeName} mới`;
    return `Chỉnh sửa ${typeName}`;
  };

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <Package size={24} />
            <h2>{getTitle()}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            {itemType === 'warehouses' && (
              <>
                <div className="form-group">
                  <label htmlFor="warehouseName">Tên kho *</label>
                  <input
                    type="text"
                    id="warehouseName"
                    name="warehouseName"
                    value={formData.warehouseName}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tên kho"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="warehouseCode">Mã kho *</label>
                  <input
                    type="text"
                    id="warehouseCode"
                    name="warehouseCode"
                    value={formData.warehouseCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã kho"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="address">Địa chỉ</label>
                  <input
                    type="text"
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập địa chỉ"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="city">Thành phố</label>
                  <input
                    type="text"
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập thành phố"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="province">Tỉnh</label>
                  <input
                    type="text"
                    id="province"
                    name="province"
                    value={formData.province}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập tỉnh"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="postalCode">Mã bưu điện</label>
                  <input
                    type="text"
                    id="postalCode"
                    name="postalCode"
                    value={formData.postalCode}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập mã bưu điện"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="phone">Số điện thoại</label>
                  <input
                    type="tel"
                    id="phone"
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập số điện thoại"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="email">Email</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập email"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="capacity">Sức chứa *</label>
                  <input
                    type="number"
                    id="capacity"
                    name="capacity"
                    value={formData.capacity}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập sức chứa"
                    min="0"
                  />
                </div>
              </>
            )}

            {itemType === 'vehicles' && (
              <>
                <div className="form-group">
                  <label htmlFor="variantId">Dòng xe *</label>
                  <select
                    id="variantId"
                    name="variantId"
                    value={formData.variantId}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="">Chọn dòng xe</option>
                    {variants.map((variant) => (
                      <option key={variant.variantId} value={variant.variantId}>
                        {variant.variantName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="dealerId">Đại lý *</label>
                  <select
                    id="dealerId"
                    name="dealerId"
                    value={formData.dealerId}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="">Chọn đại lý</option>
                    {dealers.map((dealer) => (
                      <option key={dealer.dealerId} value={dealer.dealerId}>
                        {dealer.dealerName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="quantity">Số lượng *</label>
                  <input
                    type="number"
                    id="quantity"
                    name="quantity"
                    value={formData.quantity}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập số lượng"
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="unitPrice">Đơn giá (VNĐ) *</label>
                  <input
                    type="number"
                    id="unitPrice"
                    name="unitPrice"
                    value={formData.unitPrice}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập đơn giá"
                    min="0"
                    step="1000000"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="condition">Tình trạng</label>
                  <select
                    id="condition"
                    name="condition"
                    value={formData.condition}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-select"
                  >
                    <option value="NEW">Mới</option>
                    <option value="USED">Đã sử dụng</option>
                    <option value="DEMO">Demo</option>
                    <option value="DAMAGED">Hư hỏng</option>
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="location">Vị trí</label>
                  <input
                    type="text"
                    id="location"
                    name="location"
                    value={formData.location}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-input"
                    placeholder="Nhập vị trí"
                  />
                </div>

                <div className="form-group full-width">
                  <label htmlFor="notes">Ghi chú</label>
                  <textarea
                    id="notes"
                    name="notes"
                    value={formData.notes}
                    onChange={handleInputChange}
                    disabled={mode === 'view'}
                    className="form-textarea"
                    placeholder="Nhập ghi chú"
                    rows="3"
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
          </div>

          {mode === 'view' && item && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {item.createdAt ? new Date(item.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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

export default InventoryModal;
