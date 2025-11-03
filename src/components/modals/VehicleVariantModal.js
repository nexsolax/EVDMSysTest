import React, { useState, useEffect } from 'react';
import { X, Settings, DollarSign } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import VehicleVariantForm from '../forms/VehicleVariantForm';
import './Modal.css';

const VehicleVariantModal = ({ variant, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [variantData, setVariantData] = useState(variant);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setVariantData(null);
      } else if (variant) {
        // Đối với view mode, luôn load từ API để đảm bảo có đầy đủ thông tin
        if (mode === 'view') {
          console.log('View mode: loading full details from API to ensure all fields are present');
          loadVariantDetails();
        } else if (mode === 'edit') {
          // Edit mode: chỉ load nếu thiếu dữ liệu cơ bản
          const hasRequiredFields = variant.variantName && (variant.modelId || variant.model?.modelId);
          if (hasRequiredFields) {
            // Data đã có thông tin cơ bản, sử dụng luôn không cần load lại
            console.log('Using existing variant data for edit, skipping API call');
            setVariantData(variant);
          } else {
            // Thiếu data cơ bản, cần load từ API
            console.log('Variant data incomplete, loading from API');
            loadVariantDetails();
          }
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, variant, mode]);

  const loadVariantDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getVariant(variant.variantId);
      console.log('API response for variant:', response.data);
      setVariantData(response.data);
    } catch (error) {
      console.error('Error loading variant details:', error);
      console.warn('API failed, using existing variant data:', variant);
      // Nếu API lỗi nhưng có data từ table, sử dụng data đó
      // Chỉ show warning nếu đang ở view mode, không show error vì có thể do server tạm thời
      if (mode === 'view') {
        console.warn('Using existing variant data for view mode');
        setVariantData(variant);
        // Không show toast error trong view mode để tránh làm phiền user
        // Chỉ log để debug
      } else {
        setVariantData(variant);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;

    try {
      setLoading(true);
      console.log('Submitting variant form data:', formData);
      console.log('Mode:', mode);
      
      if (mode === 'create') {
        await onSave(formData);
      } else {
        console.log('Updating variant ID:', variant?.variantId);
        await onSave(variant?.variantId, formData);
      }
      
      // Sau khi lưu thành công, chỉ đóng modal
      // Data sẽ được reload trong bảng qua loadData() trong VehicleManagement
      onClose();
    } catch (error) {
      console.error('Error saving variant:', error);
      console.error('Error response:', error.response);
      toast.error(error.response?.data?.message || 'Không thể lưu phiên bản');
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
            <Settings size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết phiên bản' : 
               mode === 'create' ? 'Thêm phiên bản mới' : 
               'Chỉnh sửa phiên bản'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        {mode === 'view' && variantData?.priceBase && (
          <div className="modal-info">
            <div className="info-item">
              <DollarSign size={16} />
              <span>Giá cơ bản: {new Intl.NumberFormat('vi-VN').format(variantData.priceBase || 'N/A') + (variantData.priceBase ? ' VNĐ' : '')}</span>
            </div>
          </div>
        )}

        <VehicleVariantForm
          variant={variantData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default VehicleVariantModal;
