import React, { useState, useEffect } from 'react';
import { X, User } from 'lucide-react';
import { customerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import CustomerForm from '../forms/CustomerForm';
import './Modal.css';

const CustomerModal = ({ customer, isOpen, onClose, onSave, mode = 'view' }) => {
  const [loading, setLoading] = useState(false);
  const [customerData, setCustomerData] = useState(customer);

  useEffect(() => {
    if (isOpen && customer && mode === 'edit') {
      loadCustomerDetails();
    } else if (isOpen && customer) {
      setCustomerData(customer);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, customer, mode]);

  const loadCustomerDetails = async () => {
    try {
      setLoading(true);
      const response = await customerAPI.getCustomer(customer.customerId);
      setCustomerData(response.data);
    } catch (error) {
      console.error('Error loading customer details:', error);
      toast.error('Không thể tải thông tin khách hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    if (mode === 'view') return;

    try {
      setLoading(true);
      await onSave(customer?.customerId, formData);
      onClose();
    } catch (error) {
      console.error('Error saving customer:', error);
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
            <User size={24} />
            <h2>
              {mode === 'create' ? 'Tạo khách hàng mới' :
               mode === 'view' ? 'Xem chi tiết khách hàng' : 
               'Chỉnh sửa khách hàng'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <CustomerForm
          customer={customerData}
          mode={mode}
          onSubmit={handleSubmit}
          onCancel={onClose}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default CustomerModal;
