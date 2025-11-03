import React, { useState, useEffect } from 'react';
import { X, Palette } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import VehicleColorForm from '../forms/VehicleColorForm';
import './Modal.css';

const VehicleColorModal = ({ color, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [colorData, setColorData] = useState(color);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setColorData(null);
      } else if (mode === 'edit' && color) {
        // Kiểm tra xem color data đã đầy đủ chưa
        const hasRequiredFields = color.colorName;
        if (hasRequiredFields) {
          // Data đã đầy đủ, sử dụng luôn không cần load lại
          console.log('Using existing color data, skipping API call');
          setColorData(color);
        } else {
          // Thiếu data, cần load từ API
          console.log('Color data incomplete, loading from API');
          loadColorDetails();
        }
      } else if (color) {
        setColorData(color);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, color, mode]);

  const loadColorDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getColor(color.colorId);
      setColorData(response.data);
    } catch (error) {
      console.error('Error loading color details:', error);
      console.warn('API failed, using existing color data:', color);
      // Nếu API lỗi nhưng có data từ table, sử dụng data đó
      setColorData(color);
      toast.error('Không thể tải thông tin chi tiết từ server. Đang sử dụng dữ liệu hiện có.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;

    try {
      setLoading(true);
      if (mode === 'create') {
        await onSave(formData);
      } else {
        await onSave(color?.colorId, formData);
      }
      onClose();
    } catch (error) {
      console.error('Error saving color:', error);
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
            <Palette size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết màu sắc' : 
               mode === 'create' ? 'Thêm màu sắc mới' : 
               'Chỉnh sửa màu sắc'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>


        <VehicleColorForm
          color={colorData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default VehicleColorModal;
