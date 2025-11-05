import React, { useState, useEffect } from 'react';
import { X, Save, ShoppingCart, Plus, Trash2 } from 'lucide-react';
import { dealerOrderAPI, vehicleAPI, dealerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const DealerOrderModal = ({ order, isOpen, onClose, onSave, mode = 'create' }) => {
  const [formData, setFormData] = useState({
    dealerId: '',
    evmStaffId: '',
    orderDate: new Date().toISOString().split('T')[0],
    expectedDeliveryDate: '',
    orderType: 'PURCHASE',
    priority: 'NORMAL',
    paymentTerms: 'NET_30',
    deliveryTerms: 'FOB_FACTORY',
    notes: '',
    items: []
  });
  const [loading, setLoading] = useState(false);
  const [variants, setVariants] = useState([]);
  const [colors, setColors] = useState([]);
  const [dealers, setDealers] = useState([]);
  const [currentItem, setCurrentItem] = useState({
    variantId: '',
    colorId: '',
    quantity: 1,
    unitPrice: '',
    discountPercentage: 0,
    notes: ''
  });

  useEffect(() => {
    if (isOpen) {
      loadDropdownData();
      if (mode === 'create') {
        resetForm();
      } else if (order) {
        loadOrderDetails();
      }
    }
  }, [isOpen, order, mode]);

  const loadDropdownData = async () => {
    try {
      const [variantsRes, colorsRes, dealersRes] = await Promise.all([
        vehicleAPI.getVariants(),
        vehicleAPI.getColors(),
        dealerAPI.getDealers()
      ]);
      setVariants(variantsRes.data || []);
      setColors(colorsRes.data || []);
      setDealers(dealersRes.data || []);
    } catch (error) {
      console.error('Error loading dropdown data:', error);
    }
  };

  const resetForm = () => {
    setFormData({
      dealerId: '',
      evmStaffId: '',
      orderDate: new Date().toISOString().split('T')[0],
      expectedDeliveryDate: '',
      orderType: 'PURCHASE',
      priority: 'NORMAL',
      paymentTerms: 'NET_30',
      deliveryTerms: 'FOB_FACTORY',
      notes: '',
      items: []
    });
    setCurrentItem({
      variantId: '',
      colorId: '',
      quantity: 1,
      unitPrice: '',
      discountPercentage: 0,
      notes: ''
    });
  };

  const loadOrderDetails = async () => {
    if (!order?.dealerOrderId) return;
    try {
      setLoading(true);
      const response = await dealerOrderAPI.getDealerOrder(order.dealerOrderId);
      const orderData = response.data;
      setFormData({
        dealerId: orderData.dealerId || '',
        evmStaffId: orderData.evmStaffId || '',
        orderDate: orderData.orderDate || '',
        expectedDeliveryDate: orderData.expectedDeliveryDate || '',
        orderType: orderData.orderType || 'PURCHASE',
        priority: orderData.priority || 'NORMAL',
        paymentTerms: orderData.paymentTerms || 'NET_30',
        deliveryTerms: orderData.deliveryTerms || 'FOB_FACTORY',
        notes: orderData.notes || '',
        items: orderData.items || []
      });
    } catch (error) {
      console.error('Error loading order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleItemChange = (e) => {
    const { name, value } = e.target;
    setCurrentItem(prev => ({ ...prev, [name]: value }));
  };

  const handleAddItem = () => {
    if (!currentItem.variantId || !currentItem.colorId || !currentItem.quantity || !currentItem.unitPrice) {
      toast.error('Vui lòng điền đầy đủ thông tin sản phẩm');
      return;
    }

    setFormData(prev => ({
      ...prev,
      items: [...prev.items, {
        variantId: parseInt(currentItem.variantId),
        colorId: parseInt(currentItem.colorId),
        quantity: parseInt(currentItem.quantity),
        unitPrice: parseFloat(currentItem.unitPrice),
        discountPercentage: parseFloat(currentItem.discountPercentage) || 0,
        notes: currentItem.notes || ''
      }]
    }));

    // Reset current item
    setCurrentItem({
      variantId: '',
      colorId: '',
      quantity: 1,
      unitPrice: '',
      discountPercentage: 0,
      notes: ''
    });
  };

  const handleRemoveItem = (index) => {
    setFormData(prev => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index)
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    if (!formData.dealerId) {
      toast.error('Vui lòng chọn đại lý');
      return;
    }

    if (formData.items.length === 0) {
      toast.error('Vui lòng thêm ít nhất một sản phẩm');
      return;
    }

    try {
      setLoading(true);
      await onSave(null, formData);
      toast.success('Tạo đơn hàng thành công');
      onClose();
      resetForm();
    } catch (error) {
      console.error('Error saving order:', error);
      toast.error(error.response?.data?.error || 'Không thể tạo đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container modal-large">
        <div className="modal-header">
          <div className="modal-title">
            <ShoppingCart size={24} />
            <h2>{mode === 'create' ? 'Tạo đơn hàng mới' : 'Chi tiết đơn hàng'}</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="dealerId">Đại lý *</label>
              <select
                id="dealerId"
                name="dealerId"
                className="form-select"
                value={formData.dealerId}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                required
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
              <label htmlFor="orderDate">Ngày đặt hàng *</label>
              <input
                id="orderDate"
                name="orderDate"
                type="date"
                className="form-input"
                value={formData.orderDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="expectedDeliveryDate">Ngày giao dự kiến</label>
              <input
                id="expectedDeliveryDate"
                name="expectedDeliveryDate"
                type="date"
                className="form-input"
                value={formData.expectedDeliveryDate}
                onChange={handleInputChange}
                disabled={mode === 'view'}
              />
            </div>

            <div className="form-group">
              <label htmlFor="orderType">Loại đơn hàng</label>
              <select
                id="orderType"
                name="orderType"
                className="form-select"
                value={formData.orderType}
                onChange={handleInputChange}
                disabled={mode === 'view'}
              >
                <option value="PURCHASE">Mua hàng</option>
                <option value="RESERVE">Đặt trước</option>
                <option value="SAMPLE">Mẫu</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="priority">Độ ưu tiên</label>
              <select
                id="priority"
                name="priority"
                className="form-select"
                value={formData.priority}
                onChange={handleInputChange}
                disabled={mode === 'view'}
              >
                <option value="LOW">Thấp</option>
                <option value="NORMAL">Bình thường</option>
                <option value="HIGH">Cao</option>
                <option value="URGENT">Khẩn cấp</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="paymentTerms">Điều khoản thanh toán</label>
              <select
                id="paymentTerms"
                name="paymentTerms"
                className="form-select"
                value={formData.paymentTerms}
                onChange={handleInputChange}
                disabled={mode === 'view'}
              >
                <option value="NET_15">NET 15</option>
                <option value="NET_30">NET 30</option>
                <option value="NET_45">NET 45</option>
                <option value="NET_60">NET 60</option>
                <option value="CASH_ON_DELIVERY">Trả khi nhận hàng</option>
                <option value="ADVANCE_PAYMENT">Thanh toán trước</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="deliveryTerms">Điều khoản giao hàng</label>
              <select
                id="deliveryTerms"
                name="deliveryTerms"
                className="form-select"
                value={formData.deliveryTerms}
                onChange={handleInputChange}
                disabled={mode === 'view'}
              >
                <option value="FOB_FACTORY">FOB Factory</option>
                <option value="FOB_DESTINATION">FOB Destination</option>
                <option value="EX_WORKS">EX Works</option>
                <option value="CIF">CIF</option>
                <option value="DDP">DDP</option>
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="notes">Ghi chú</label>
              <textarea
                id="notes"
                name="notes"
                className="form-input"
                rows={3}
                value={formData.notes}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                placeholder="Ghi chú về đơn hàng..."
              />
            </div>
          </div>

          {/* Add Items Section */}
          {mode !== 'view' && (
            <div className="items-section">
              <h3>Thêm sản phẩm</h3>
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="itemVariantId">Phiên bản xe *</label>
                  <select
                    id="itemVariantId"
                    name="variantId"
                    className="form-select"
                    value={currentItem.variantId}
                    onChange={handleItemChange}
                  >
                    <option value="">Chọn phiên bản</option>
                    {variants.map((variant) => (
                      <option key={variant.variantId} value={variant.variantId}>
                        {variant.model?.brand?.brandName} {variant.model?.modelName} {variant.variantName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="itemColorId">Màu *</label>
                  <select
                    id="itemColorId"
                    name="colorId"
                    className="form-select"
                    value={currentItem.colorId}
                    onChange={handleItemChange}
                  >
                    <option value="">Chọn màu</option>
                    {colors.map((color) => (
                      <option key={color.colorId} value={color.colorId}>
                        {color.colorName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="itemQuantity">Số lượng *</label>
                  <input
                    id="itemQuantity"
                    name="quantity"
                    type="number"
                    min="1"
                    className="form-input"
                    value={currentItem.quantity}
                    onChange={handleItemChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="itemUnitPrice">Đơn giá (VNĐ) *</label>
                  <input
                    id="itemUnitPrice"
                    name="unitPrice"
                    type="number"
                    min="0"
                    step="1000"
                    className="form-input"
                    value={currentItem.unitPrice}
                    onChange={handleItemChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="itemDiscount">Giảm giá (%)</label>
                  <input
                    id="itemDiscount"
                    name="discountPercentage"
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                    className="form-input"
                    value={currentItem.discountPercentage}
                    onChange={handleItemChange}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="itemNotes">Ghi chú</label>
                  <input
                    id="itemNotes"
                    name="notes"
                    type="text"
                    className="form-input"
                    value={currentItem.notes}
                    onChange={handleItemChange}
                    placeholder="Ghi chú về sản phẩm..."
                  />
                </div>
              </div>

              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleAddItem}
              >
                <Plus size={20} />
                Thêm sản phẩm
              </button>
            </div>
          )}

          {/* Items List */}
          {formData.items.length > 0 && (
            <div className="items-list">
              <h3>Danh sách sản phẩm ({formData.items.length})</h3>
              <table className="table">
                <thead>
                  <tr>
                    <th>Variant</th>
                    <th>Màu</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Giảm giá</th>
                    <th>Thành tiền</th>
                    {mode !== 'view' && <th>Thao tác</th>}
                  </tr>
                </thead>
                <tbody>
                  {formData.items.map((item, index) => {
                    const variant = variants.find(v => v.variantId === item.variantId);
                    const color = colors.find(c => c.colorId === item.colorId);
                    const discountAmount = (item.unitPrice * item.quantity * item.discountPercentage) / 100;
                    const totalPrice = (item.unitPrice * item.quantity) - discountAmount;

                    return (
                      <tr key={index}>
                        <td>
                          {variant 
                            ? `${variant.model?.brand?.brandName} ${variant.model?.modelName} ${variant.variantName}`
                            : `Variant ID: ${item.variantId}`}
                        </td>
                        <td>{color ? color.colorName : `Color ID: ${item.colorId}`}</td>
                        <td>{item.quantity}</td>
                        <td>{Number(item.unitPrice).toLocaleString('vi-VN')} VNĐ</td>
                        <td>{item.discountPercentage}%</td>
                        <td>{Number(totalPrice).toLocaleString('vi-VN')} VNĐ</td>
                        {mode !== 'view' && (
                          <td>
                            <button
                              type="button"
                              className="btn btn-sm btn-danger"
                              onClick={() => handleRemoveItem(index)}
                            >
                              <Trash2 size={16} />
                            </button>
                          </td>
                        )}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            {mode === 'create' && (
              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={20} />
                {loading ? 'Đang tạo...' : 'Tạo đơn hàng'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default DealerOrderModal;

