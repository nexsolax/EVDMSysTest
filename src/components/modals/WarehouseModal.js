import React, { useEffect, useState } from 'react';
import { X, Building2 } from 'lucide-react';
import { warehouseAPI } from '../../services/api';
import toast from 'react-hot-toast';
import WarehouseForm from '../forms/WarehouseForm';
import './Modal.css';

const WarehouseModal = ({ warehouse, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [warehouseData, setWarehouseData] = useState(warehouse);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'create') {
        setWarehouseData(null);
      } else if (warehouse && mode === 'edit') {
        loadWarehouseDetails();
      } else if (warehouse) {
        setWarehouseData(warehouse);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, warehouse, mode]);

  const loadWarehouseDetails = async () => {
    try {
      setLoading(true);
      const res = await warehouseAPI.getWarehouse(warehouse.warehouseId);
      setWarehouseData(res.data);
    } catch (e) {
      console.error('Error loading warehouse:', e);
      toast.error('Không thể tải thông tin kho');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;
    try {
      setLoading(true);
      await onSave(mode === 'create' ? null : warehouse?.warehouseId, formData);
      onClose();
    } catch (e) {
      console.error('Error saving warehouse:', e);
      // Error is handled by parent component
      throw e;
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
            <Building2 size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết kho' : 
               mode === 'create' ? 'Thêm kho mới' : 
               'Chỉnh sửa kho'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <WarehouseForm
          warehouse={warehouseData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default WarehouseModal;
