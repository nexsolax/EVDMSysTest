import React, { useState, useEffect } from 'react';
import { X, Save, User, Calendar } from 'lucide-react';
import { userAPI, dealerAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const UserModal = ({ user, isOpen, onClose, onSave, mode = 'view' }) => {
  const [formData, setFormData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    role: '',
    dealerName: '', // Use dealerName instead of dealerId
    isActive: true
  });
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState([]);
  const [dealers, setDealers] = useState([]);

  useEffect(() => {
    if (isOpen) {
      const initializeModal = async () => {
        await Promise.all([
          loadRoles(), // Load available roles from API
          loadDealers() // Load dealers for dropdown
        ]);
        
        if (mode === 'create') {
          // Reset form for new user
          setFormData({
            username: '',
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            password: 'password123',
            role: '',
            dealerName: '',
            isActive: true
          });
        } else if (mode === 'edit' && user) {
          await loadUserDetails();
        } else if (mode === 'view' && user) {
          // Backend uses UserType enum, normalize it to lowercase
          const normalizeUserType = (userType) => {
            if (!userType) return '';
            const typeMapping = {
              'ADMIN': 'admin',
              'EVM_STAFF': 'evm_staff',
              'DEALER_MANAGER': 'dealer_manager',
              'DEALER_STAFF': 'dealer_staff'
            };
            return typeMapping[userType] || userType.toLowerCase();
          };
          
          setFormData({
            username: user.username || '',
            firstName: user.firstName || '',
            lastName: user.lastName || '',
            email: user.email || '',
            phone: user.phone || '',
            password: '', // Don't show password in view mode
            role: normalizeUserType(user.userType) || user.roleString || user.role?.roleName || '',
            dealerName: user.dealer?.dealerName || '',
            isActive: user.isActive || false
          });
        }
      };
      
      initializeModal();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, user, mode]);

  const loadRoles = async () => {
    try {
      // Backend uses UserType enum, not UserRole entity
      // Use hardcoded UserType enum values
      setRoles([
        { roleId: 1, roleName: 'admin' },
        { roleId: 2, roleName: 'evm_staff' },
        { roleId: 3, roleName: 'dealer_manager' },
        { roleId: 4, roleName: 'dealer_staff' }
      ]);
    } catch (error) {
      console.error('Error loading roles:', error);
      // Fallback to hardcoded roles if API fails
      setRoles([
        { roleId: 1, roleName: 'admin' },
        { roleId: 2, roleName: 'evm_staff' },
        { roleId: 3, roleName: 'dealer_manager' },
        { roleId: 4, roleName: 'dealer_staff' }
      ]);
    }
  };

  const loadDealers = async () => {
    try {
      const response = await dealerAPI.getDealersOptions();
      setDealers(response.data || []);
    } catch (error) {
      console.error('Error loading dealers:', error);
      // Fallback: try to get full dealers list
      try {
        const fallbackResponse = await dealerAPI.getDealers();
        const dealersList = fallbackResponse.data || [];
        setDealers(dealersList.map(d => ({
          dealerId: d.dealerId,
          dealerName: d.dealerName,
          dealerCode: d.dealerCode || ''
        })));
      } catch (fallbackError) {
        console.error('Error loading dealers fallback:', fallbackError);
        setDealers([]);
      }
    }
  };

  const loadUserDetails = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getUser(user.userId);
      const userData = response.data;
        // Backend uses UserType enum, normalize it to lowercase
        const normalizeUserType = (userType) => {
          if (!userType) return '';
          const typeMapping = {
            'ADMIN': 'admin',
            'EVM_STAFF': 'evm_staff',
            'DEALER_MANAGER': 'dealer_manager',
            'DEALER_STAFF': 'dealer_staff'
          };
          return typeMapping[userType] || userType.toLowerCase();
        };
        
        setFormData({
          username: userData.username || '',
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          email: userData.email || '',
          phone: userData.phone || '',
          password: '', // Don't load password for edit mode
          role: normalizeUserType(userData.userType) || userData.roleString || userData.role?.roleName || '',
          dealerName: userData.dealer?.dealerName || '',
          isActive: userData.isActive || false
        });
    } catch (error) {
      console.error('Error loading user details:', error);
      toast.error('Không thể tải thông tin người dùng');
    } finally {
      setLoading(false);
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

    // Validation
    if (mode === 'create' && !formData.username.trim()) {
      toast.error('Tên đăng nhập là bắt buộc');
      return;
    }
    if (!formData.firstName.trim()) {
      toast.error('Họ là bắt buộc');
      return;
    }
    if (!formData.lastName.trim()) {
      toast.error('Tên là bắt buộc');
      return;
    }
    if (!formData.email.trim()) {
      toast.error('Email là bắt buộc');
      return;
    }
    if (!formData.role) {
      toast.error('Vai trò là bắt buộc');
      return;
    }
    
    // Validate dealer: Admin không cần, các role khác bắt buộc
    const isAdmin = formData.role === 'admin';
    const requiresDealer = !isAdmin; // EVM_STAFF, DEALER_MANAGER, DEALER_STAFF cần dealer
    
    if (requiresDealer && !formData.dealerName?.trim()) {
      toast.error('Vui lòng chọn đại lý (bắt buộc cho role này)');
      return;
    }
    
    // Password validation is not needed since we use default password

    try {
      setLoading(true);
      
      // Prepare data for backend - match UserRequest DTO structure
      // Backend uses UserType enum (ADMIN, EVM_STAFF, DEALER_MANAGER, DEALER_STAFF)
      // Convert lowercase role to uppercase enum
      const roleToUserType = (role) => {
        const mapping = {
          'admin': 'ADMIN',
          'evm_staff': 'EVM_STAFF',
          'dealer_manager': 'DEALER_MANAGER',
          'dealer_staff': 'DEALER_STAFF'
        };
        return mapping[role] || role?.toUpperCase();
      };
      
      // Required fields
      const submitData = {
        username: formData.username.trim(),
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        password: formData.password,
        userType: roleToUserType(formData.role), // Use userType enum instead of roleString
        isActive: formData.isActive
      };
      
      // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
      if (formData.phone?.trim()) {
        submitData.phone = formData.phone.trim();
      }
      if (formData.dealerName?.trim() && !isAdmin) {
        submitData.dealerName = formData.dealerName.trim(); // Send dealerName (backend will find by name)
      }
      
      // Log data being sent
      console.log('Sending user data:', submitData);
      console.log('Mode:', mode);
      console.log('Role being sent:', formData.role);
      
      if (mode === 'create') {
        // For create mode, call createUser API
        await onSave(null, submitData);
      } else {
        // For edit mode, call updateUser API
        console.log('User ID:', user.userId);
        await onSave(user.userId, submitData);
      }
      // Modal will be closed by parent component after successful save
    } catch (error) {
      console.error('Error saving user:', error);
      
      // Log detailed error information
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        // Show specific error message
        if (error.response.data && error.response.data.message) {
          const errorMessage = error.response.data.message;
          if (errorMessage.includes('username') && errorMessage.includes('already exists')) {
            toast.error('Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.');
          } else if (errorMessage.includes('email') && errorMessage.includes('already exists')) {
            toast.error('Email đã tồn tại. Vui lòng sử dụng email khác.');
          } else {
            toast.error(`Lỗi: ${errorMessage}`);
          }
        } else {
          toast.error(mode === 'create' ? 'Không thể tạo người dùng' : 'Không thể cập nhật người dùng');
        }
      } else {
        toast.error('Lỗi kết nối đến server');
      }
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
              {mode === 'view' ? 'Xem chi tiết người dùng' : 
               mode === 'create' ? 'Thêm người dùng mới' : 
               'Chỉnh sửa người dùng'}
            </h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-grid">
            {mode === 'create' && (
              <div className="form-group">
                <label htmlFor="username">Tên đăng nhập</label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="Nhập tên đăng nhập"
                />
              </div>
            )}

            <div className="form-group">
              <label htmlFor="firstName">Họ</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="lastName">Tên</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                disabled={mode === 'view'}
                className="form-input"
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
              />
            </div>


            <div className="form-group">
              <label htmlFor="role">Vai trò</label>
              {mode === 'view' ? (
                <div className="form-display">
                  {(() => {
                    const roleName = formData.role;
                    if (roleName === 'admin') return 'Admin';
                    if (roleName === 'evm_staff') return 'EVM Staff';
                    if (roleName === 'dealer_manager') return 'Dealer Manager';
                    if (roleName === 'dealer_staff') return 'Dealer Staff';
                    return roleName ? roleName.charAt(0).toUpperCase() + roleName.slice(1).replace('_', ' ') : 'N/A';
                  })()}
                </div>
              ) : (
                <select
                  id="role"
                  name="role"
                  value={formData.role}
                  onChange={(e) => {
                    handleInputChange(e);
                    // Clear dealerName when switching to admin
                    if (e.target.value === 'admin') {
                      setFormData(prev => ({ ...prev, dealerName: '' }));
                    }
                  }}
                  className="form-select"
                >
                  <option value="" disabled>Chọn vai trò</option>
                  {roles.map((role) => (
                    <option key={role.roleId} value={role.roleName}>
                      {role.roleName === 'admin' && 'Admin'}
                      {role.roleName === 'evm_staff' && 'EVM Staff'}
                      {role.roleName === 'dealer_manager' && 'Dealer Manager'}
                      {role.roleName === 'dealer_staff' && 'Dealer Staff'}
                      {!['admin', 'evm_staff', 'dealer_manager', 'dealer_staff'].includes(role.roleName) && 
                        role.roleName.charAt(0).toUpperCase() + role.roleName.slice(1).replace('_', ' ')
                      }
                    </option>
                  ))}
                </select>
              )}
            </div>

            {/* Dealer Selection - Hiển thị khi không phải admin */}
            {formData.role && formData.role !== 'admin' && (
              <div className="form-group">
                <label htmlFor="dealerName">
                  Đại lý {mode === 'view' ? '' : '*'}
                </label>
                {mode === 'view' ? (
                  <div className="form-display">
                    {formData.dealerName || 'N/A'}
                  </div>
                ) : (
                  <select
                    id="dealerName"
                    name="dealerName"
                    value={formData.dealerName}
                    onChange={handleInputChange}
                    className="form-select"
                    required={formData.role !== 'admin'}
                  >
                    <option value="">-- Chọn đại lý --</option>
                    {dealers.map((dealer) => (
                      <option key={dealer.dealerId} value={dealer.dealerName}>
                        {dealer.dealerCode ? `${dealer.dealerCode}: ` : ''}{dealer.dealerName}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            <div className="form-group">
              {mode === 'view' ? (
                <div className="form-display">
                  <span style={{ color: formData.isActive ? '#10b981' : '#ef4444', fontWeight: '500' }}>
                    {formData.isActive ? '✓ Đang hoạt động' : '✗ Không hoạt động'}
                  </span>
                </div>
              ) : (
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="isActive"
                    checked={formData.isActive}
                    onChange={handleInputChange}
                    className="form-checkbox"
                  />
                  <span>Đang hoạt động</span>
                </label>
              )}
            </div>
          </div>

          {mode === 'view' && user && (
            <div className="modal-info">
              <div className="info-item">
                <Calendar size={16} />
                <span>Ngày tạo: {user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</span>
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
                {loading ? 'Đang lưu...' : mode === 'create' ? 'Tạo người dùng' : 'Lưu thay đổi'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserModal;
