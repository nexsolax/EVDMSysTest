import React, { useState, useEffect } from 'react';
import { X, Package, Calendar } from 'lucide-react';
import { vehicleAPI } from '../../services/api';
import toast from 'react-hot-toast';
import VehicleModelForm from '../forms/VehicleModelForm';
import './Modal.css';

const VehicleModelModal = ({ model, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [modelData, setModelData] = useState(model);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setModelData(null);
      } else if (mode === 'edit' && model) {
        // Kiểm tra xem model data đã đầy đủ chưa
        const hasRequiredFields = model.modelName && (model.brandId || model.brand?.brandId);
        if (hasRequiredFields) {
          // Data đã đầy đủ, sử dụng luôn không cần load lại
          console.log('Using existing model data, skipping API call');
          setModelData(model);
        } else {
          // Thiếu data, cần load từ API
          console.log('Model data incomplete, loading from API');
          loadModelDetails();
        }
      } else if (model) {
        setModelData(model);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, model, mode]);

  const loadModelDetails = async () => {
    try {
      setLoading(true);
      const response = await vehicleAPI.getModel(model.modelId);
      console.log('Loaded model details:', response.data);
      setModelData(response.data);
    } catch (error) {
      console.error('Error loading model details:', error);
      console.warn('API failed, using existing model data:', model);
      // Nếu API lỗi nhưng có data từ table, sử dụng data đó
      setModelData(model);
      toast.error('Không thể tải thông tin chi tiết từ server. Đang sử dụng dữ liệu hiện có.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;

    try {
      setLoading(true);
      console.log('Modal submitting form data:', formData);
      if (mode === 'create') {
        await onSave(formData);
      } else {
        await onSave(model?.modelId, formData);
      }
      onClose();
    } catch (error) {
      console.error('Error saving model:', error);
      // Error đã được xử lý trong handleCreateModel/handleSaveModel
      // Không cần hiển thị lại error ở đây
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
              {mode === 'view' ? 'Xem chi tiết dòng xe' : 
               mode === 'create' ? 'Thêm dòng xe mới' : 
               'Chỉnh sửa dòng xe'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        {mode === 'view' && modelData?.createdAt && (
          <div className="modal-info">
            <div className="info-item">
              <Calendar size={16} />
              <span>Ngày tạo: {new Date(modelData.createdAt).toLocaleDateString('vi-VN')}</span>
            </div>
          </div>
        )}

        <VehicleModelForm
          model={modelData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default VehicleModelModal;
