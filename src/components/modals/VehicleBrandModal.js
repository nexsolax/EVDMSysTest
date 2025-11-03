import React, { useState, useEffect } from 'react';
import { X, Car, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import VehicleBrandForm from '../forms/VehicleBrandForm';
import './Modal.css';

const VehicleBrandModal = ({ brand, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [brandData, setBrandData] = useState(brand);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setBrandData(null);
      } else if (brand) {
        // Đối với view mode, luôn load từ API để đảm bảo có đầy đủ thông tin
        if (mode === 'view') {
          console.log('View mode: loading full details from API to ensure all fields are present');
          loadBrandDetails();
        } else if (mode === 'edit') {
          // Edit mode: chỉ load nếu thiếu dữ liệu cơ bản
          const hasRequiredFields = brand.brandName;
          if (hasRequiredFields) {
            // Data đã có thông tin cơ bản, sử dụng luôn không cần load lại
            console.log('Using existing brand data for edit, skipping API call');
            setBrandData(brand);
          } else {
            // Thiếu data cơ bản, cần load từ API
            console.log('Brand data incomplete, loading from API');
            loadBrandDetails();
          }
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, brand, mode]);

  const loadBrandDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getBrand(brand.brandId);
      console.log('API response for brand:', response.data);
      setBrandData(response.data);
    } catch (error) {
      console.error('Error loading brand details:', error);
      console.warn('API failed, using existing brand data:', brand);
      // Nếu API lỗi nhưng có data từ table, sử dụng data đó
      // Chỉ show warning nếu đang ở view mode, không show error vì có thể do server tạm thời
      if (mode === 'view') {
        console.warn('Using existing brand data for view mode');
        setBrandData(brand);
        // Không show toast error trong view mode để tránh làm phiền user
        // Chỉ log để debug
      } else {
        setBrandData(brand);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;

    try {
      setLoading(true);
      console.log('Submitting brand form data:', formData);
      
      if (mode === 'create') {
        await onSave(formData);
      } else {
        await onSave(brand?.brandId, formData);
      }
      
      // Sau khi lưu thành công, chỉ đóng modal
      // Data sẽ được reload trong bảng qua loadData() trong VehicleManagement
      onClose();
    } catch (error) {
      console.error('Error saving brand:', error);
      console.error('Error response:', error.response);
      toast.error(error.response?.data?.message || 'Không thể lưu thương hiệu');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {/* Brand Logo in header */}
            {brandData && (brandData.brandLogoUrl || brandData.brandLogoPath) ? (
              <img
                src={brandData.brandLogoUrl || (brandData.brandLogoPath?.startsWith('http') ? brandData.brandLogoPath : `/${brandData.brandLogoPath}`)}
                alt={brandData.brandName || 'Brand Logo'}
                style={{
                  width: '40px',
                  height: '40px',
                  objectFit: 'contain',
                  borderRadius: '4px',
                  border: '1px solid #e5e7eb'
                }}
                onError={(e) => {
                  e.target.style.display = 'none';
                  // Show Car icon if image fails
                  const carIcon = e.target.nextSibling;
                  if (carIcon) carIcon.style.display = 'flex';
                }}
              />
            ) : null}
            {(!brandData || !brandData.brandLogoUrl && !brandData.brandLogoPath) && (
              <Car size={24} />
            )}
            <h2>
              {mode === 'view' ? 'Xem chi tiết thương hiệu' : 
               mode === 'create' ? 'Thêm thương hiệu mới' : 
               'Chỉnh sửa thương hiệu'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        {mode === 'view' && brandData?.createdAt && (
          <div className="modal-info">
            <div className="info-item">
              <Calendar size={16} />
              <span>Ngày tạo: {new Date(brandData.createdAt).toLocaleDateString('vi-VN')}</span>
            </div>
          </div>
        )}

        <VehicleBrandForm
          brand={brandData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default VehicleBrandModal;
