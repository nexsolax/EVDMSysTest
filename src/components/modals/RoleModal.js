import React, { useState, useEffect } from 'react';
import { X, Save, Shield } from 'lucide-react';
import { userAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';
import './RoleModal.css';

const RoleModal = ({ role, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    roleName: '',
    description: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && role) {
      if (mode === 'edit') {
        loadRoleDetails();
      } else {
        setFormData({
          roleName: role.roleName || '',
          description: role.description || ''
        });
      }
    } else if (isOpen && mode === 'create') {
      setFormData({
        roleName: '',
        description: ''
      });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, role, mode]);

  const loadRoleDetails = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getRole(role.roleId);
      const roleData = response.data;
      setFormData({
        roleName: roleData.roleName || '',
        description: roleData.description || ''
      });
    } catch (error) {
      console.error('Error loading role details:', error);
      toast.error('Không thể tải thông tin vai trò');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'view') return;

    try {
      setLoading(true);
      
      // Log data being sent
      console.log('Sending role data:', formData);
      console.log('Mode:', mode);
      
      if (mode === 'create') {
        await onSave(null, formData);
      } else {
        await onSave(role.roleId, formData);
      }
      
    } catch (error) {
      console.error('Error saving role:', error);
      
      // Log detailed error information
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        // Show specific error message
        if (error.response.data && error.response.data.message) {
          toast.error(`Lỗi: ${error.response.data.message}`);
        } else {
          toast.error('Không thể lưu vai trò');
        }
      } else {
        toast.error('Lỗi kết nối đến server');
      }
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const errors = {};
    
    if (!formData.roleName.trim()) {
      errors.roleName = 'Tên vai trò là bắt buộc';
    }
    
    if (!formData.description.trim()) {
      errors.description = 'Mô tả là bắt buộc';
    }
    
    return Object.keys(errors).length === 0;
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <Shield size={24} />
            <h2>
              {mode === 'view' ? 'Xem chi tiết vai trò' : 
               mode === 'edit' ? 'Chỉnh sửa vai trò' : 
               'Tạo vai trò mới'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="roleName">Tên vai trò *</label>
              <input
                type="text"
                id="roleName"
                name="roleName"
                value={formData.roleName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
                placeholder="Nhập tên vai trò"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Mô tả *</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-textarea"
                placeholder="Nhập mô tả vai trò"
                rows="3"
                required
              />
            </div>

          </div>

          {mode === 'view' && (
            <div className="view-info">
              <div className="info-item">
                <strong>ID:</strong> {role?.roleId}
              </div>
              <div className="info-item">
                <strong>Ngày tạo:</strong> {role?.createdAt ? new Date(role.createdAt).toLocaleString('vi-VN') : 'N/A'}
              </div>
            </div>
          )}

          <div className="modal-actions">
            <button 
              type="button" 
              className="btn btn-secondary" 
              onClick={onClose}
            >
              {mode === 'view' ? 'Đóng' : 'Hủy'}
            </button>
            
            {mode !== 'view' && (
              <button 
                type="submit" 
                className="btn btn-primary"
                disabled={loading || !validateForm()}
              >
                {loading ? (
                  <>
                    <div className="spinner"></div>
                    Đang lưu...
                  </>
                ) : (
                  <>
                    <Save size={16} />
                    {mode === 'create' ? 'Tạo vai trò' : 'Lưu thay đổi'}
                  </>
                )}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default RoleModal;
