import React, { useState, useEffect } from 'react';
import { User, Plus, Search, Edit, Trash2, Eye } from 'lucide-react';
import { userAPI } from '../services/api';
import { getRoleBadge, getActiveBadge } from '../utils/statusBadges';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import UserModal from '../components/modals/UserModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './UserManagement.css';

const UserManagement = () => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRole, setFilterRole] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [showUserModal, setShowUserModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view', 'edit', or 'create'

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      console.log('Loading users from API...');
      const response = await userAPI.getUsers();
      console.log('Users API response:', response);
      console.log('Users API response data:', response.data);
      
      let usersData = response.data || [];
      console.log('Setting users state with:', usersData.length, 'users');
      
      // Validate and handle dealer relationship safely
      usersData = usersData.map(user => {
        // Ensure dealer is properly structured
        if (user.dealer && typeof user.dealer === 'object') {
          // Dealer is already an object - good
        } else if (user.dealerId && !user.dealer) {
          // Has dealerId but no dealer object - will show as N/A
          console.warn(`User ${user.username} has dealerId but no dealer object:`, user.dealerId);
        }
        return user;
      });
      
      // Log detailed user structure for first user to debug
      if (usersData.length > 0) {
        console.log('First user full structure:', JSON.stringify(usersData[0], null, 2));
        console.log('First user userType:', usersData[0].userType);
        console.log('First user dealer:', usersData[0].dealer);
        console.log('First user all keys:', Object.keys(usersData[0]));
      }
      
      // Backend uses UserType enum directly - no need to enrich
      
      // Log each user's role for debugging (only in development)
      if (process.env.NODE_ENV === 'development') {
        usersData.forEach(user => {
          console.log(`User ${user.username}:`, {
            userType: user.userType,
            roleString: user.roleString,
            role: user.role,
            'role.roleName': user.role?.roleName,
            dealer: user.dealer,
            normalizedUserType: normalizeUserType(user.userType),
            allKeys: Object.keys(user)
          });
        });
      }
      
      setUsers(usersData);
      
    } catch (error) {
      console.error('Error loading users:', error);
      
      // Log detailed error information
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        // Show specific error message
        if (error.response.data && error.response.data.message) {
          toast.error(`Lỗi server: ${error.response.data.message}`);
        } else if (error.response.status === 500) {
          toast.error('Lỗi server (500). Vui lòng kiểm tra backend logs hoặc liên hệ quản trị viên.');
        } else {
          toast.error(`Không thể tải danh sách người dùng (${error.response.status})`);
        }
      } else if (error.request) {
        console.error('Request was made but no response received:', error.request);
        toast.error('Không có phản hồi từ server. Vui lòng kiểm tra kết nối.');
      } else {
        console.error('Error setting up request:', error.message);
        toast.error(`Lỗi: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  // Normalize userType enum to lowercase for display
  const normalizeUserType = (userType) => {
    if (!userType) return null;
    
    // If already lowercase, return as is
    if (typeof userType === 'string' && userType === userType.toLowerCase()) {
      return userType;
    }
    
    // Map enum values (uppercase) to lowercase
    const typeMapping = {
      'ADMIN': 'admin',
      'EVM_STAFF': 'evm_staff',
      'DEALER_MANAGER': 'dealer_manager',
      'DEALER_STAFF': 'dealer_staff'
    };
    
    return typeMapping[userType] || userType.toLowerCase();
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleFilterRole = (e) => {
    setFilterRole(e.target.value);
  };

  const filteredUsers = users.filter(user => {
    const matchesSearch = 
      user.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase());
    
    // Use userType (enum) as primary source, fallback to roleString or role.roleName
    const userRole = normalizeUserType(user.userType) || user.roleString || user.role?.roleName;
    const matchesRole = !filterRole || userRole === filterRole;
    
    return matchesSearch && matchesRole;
  });

  const handleViewUser = (user) => {
    setSelectedUser(user);
    setModalMode('view');
    setShowUserModal(true);
  };

  const handleEditUser = (user) => {
    setSelectedUser(user);
    setModalMode('edit');
    setShowUserModal(true);
  };

  const handleCreateUser = () => {
    setSelectedUser(null);
    setModalMode('create');
    setShowUserModal(true);
  };

  const handleSaveUser = async (userId, userData) => {
    try {
      let response;
      
      if (userId === null) {
        // Create new user
        console.log('Creating new user with data:', userData);
        response = await userAPI.createUser(userData);
        console.log('Create response:', response.data);
        toast.success('Tạo người dùng thành công');
      } else {
        // Update existing user
        console.log('Updating user with data:', userData);
        console.log('User ID being updated:', userId);
        response = await userAPI.updateUser(userId, userData);
        console.log('Update response:', response.data);
        toast.success('Cập nhật người dùng thành công');
      }
      
      // Close modal first
      setShowUserModal(false);
      setSelectedUser(null);
      
      // Then reload users data
      console.log('Reloading users data...');
      await loadUsers();
      console.log('Users data reloaded successfully');
      
    } catch (error) {
      console.error('Error saving user:', error);
      
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        
        if (error.response.data && error.response.data.message) {
          toast.error(`Lỗi: ${error.response.data.message}`);
        } else if (error.response.status === 400) {
          toast.error('Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.');
        } else if (error.response.status === 401) {
          toast.error('Không có quyền thực hiện thao tác này');
        } else if (error.response.status === 404) {
          toast.error('Không tìm thấy người dùng');
        } else {
          toast.error(userId === null ? 'Không thể tạo người dùng' : 'Không thể cập nhật người dùng');
        }
      } else {
        toast.error('Lỗi kết nối đến server');
      }
      
      throw error;
    }
  };

  const handleDeleteUser = async (user) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa người dùng "${user.username}"?`)) {
      try {
        await userAPI.deleteUser(user.userId);
        toast.success('Xóa người dùng thành công');
        loadUsers();
      } catch (error) {
        console.error('Error deleting user:', error);
        toast.error('Không thể xóa người dùng');
      }
    }
  };


  const handleChangePassword = async (user) => {
    const newPassword = window.prompt(`Đặt mật khẩu mới cho người dùng "${user.username}":`);
    if (newPassword && newPassword.length >= 6) {
      try {
        await userAPI.resetPassword(user.userId, newPassword);
        toast.success('Đặt lại mật khẩu thành công');
      } catch (error) {
        console.error('Error changing password:', error);
        toast.error('Không thể đặt lại mật khẩu');
      }
    } else if (newPassword) {
      toast.error('Mật khẩu phải có ít nhất 6 ký tự');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách người dùng..." />;
  }

  return (
    <div className="user-management">
      <div className="page-header">
        <div className="page-title">
          <User className="title-icon" />
          <h1>Quản lý người dùng</h1>
        </div>
        <p>Quản lý tài khoản người dùng và phân quyền</p>
      </div>

      <div className="content">
        {/* Stats Cards */}
        <div className="stats-section">
          <div className="stat-card">
            <div className="stat-icon">
              <User size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{users.length}</div>
              <div className="stat-label">Tổng người dùng</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">
              <User size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{users.filter(u => u.isActive).length}</div>
              <div className="stat-label">Đang hoạt động</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">
              <User size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{users.filter(u => !u.isActive).length}</div>
              <div className="stat-label">Không hoạt động</div>
            </div>
          </div>
        </div>

        <div className="section-header">
          <h2>Danh sách người dùng</h2>
          <button className="btn btn-primary btn-sm" onClick={handleCreateUser}>
            <Plus size={16} />
            Thêm người dùng
          </button>
        </div>

        <div className="search-bar">
          <Search className="search-icon" />
          <input 
            type="text" 
            placeholder="Tìm kiếm người dùng..." 
            value={searchTerm}
            onChange={handleSearch}
          />
          <select 
            className="form-select"
            value={filterRole}
            onChange={handleFilterRole}
          >
            <option value="">Tất cả vai trò</option>
            <option value="admin">Admin</option>
            <option value="evm_staff">EVM Staff</option>
            <option value="dealer_manager">Dealer Manager</option>
            <option value="dealer_staff">Dealer Staff</option>
          </select>
        </div>

        <div 
          className="table-container"
          style={{
            overflowX: 'auto',
            overflowY: 'visible',
            width: '100%',
            maxWidth: '100%'
          }}
        >
          <table 
            className="table"
            style={{
              minWidth: '1200px',
              width: '100%'
            }}
          >
            <thead>
              <tr>
                <th>Tên đăng nhập</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Vai trò</th>
                <th>Đại lý</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="9" className="text-center">
                    <div className="empty-state">
                      <User size={48} />
                      <p>Không tìm thấy người dùng nào</p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.userId}>
                    <td>
                      <div className="user-info-cell">
                        <div className="user-avatar-small">
                          {user.username?.charAt(0).toUpperCase()}
                        </div>
                        <span className="username">{user.username}</span>
                      </div>
                    </td>
                    <td>
                      {user.firstName && user.lastName 
                        ? `${user.firstName} ${user.lastName}`
                        : 'N/A'
                      }
                    </td>
                    <td>{user.email || 'N/A'}</td>
                    <td>{user.phone || 'N/A'}</td>
                    <td>
                      {(() => { 
                        // Backend uses UserType enum (ADMIN, EVM_STAFF, DEALER_MANAGER, DEALER_STAFF)
                        // Priority: userType (enum) > roleString > role.roleName
                        const roleName = normalizeUserType(user.userType) || user.roleString || user.role?.roleName;
                        
                        // Log for debugging if role is missing
                        if (!roleName) {
                          console.warn('User role not found for user:', user.username, {
                            userType: user.userType,
                            roleString: user.roleString,
                            role: user.role,
                            allUserKeys: Object.keys(user)
                          });
                        }
                        
                        const b = getRoleBadge(roleName); 
                        return (<span className={`badge ${b.class}`}>{b.text}</span>); 
                      })()}
                    </td>
                    <td>
                      {(() => {
                        // Admin không cần dealer
                        const roleName = normalizeUserType(user.userType) || user.roleString || user.role?.roleName;
                        if (roleName === 'admin') {
                          return <span className="text-muted">-</span>;
                        }
                        // Hiển thị dealer name hoặc code
                        return user.dealer?.dealerName || user.dealer?.dealerCode || 'N/A';
                      })()}
                    </td>
                    <td>
                      {(() => { const b = getActiveBadge(user.isActive); return (<span className={`badge ${b.class}`}>{b.text}</span>); })()}
                    </td>
                    <td>{formatDate(user.createdAt)}</td>
                    <td>
                             <div className="action-buttons">
                               <button 
                                 className="btn btn-sm btn-outline"
                                 onClick={() => handleViewUser(user)}
                                 title="Xem chi tiết"
                               >
                                 <Eye size={16} />
                               </button>
                               <button 
                                 className="btn btn-sm btn-outline"
                                 onClick={() => handleEditUser(user)}
                                 title="Chỉnh sửa"
                               >
                                 <Edit size={16} />
                               </button>
                               {user.userId !== currentUser?.userId && (
                                 <>
                                   <button 
                                     className="btn btn-sm btn-info"
                                     onClick={() => handleChangePassword(user)}
                                     title="Đặt lại mật khẩu"
                                   >
                                     Đặt lại MK
                                   </button>
                                   <button 
                                     className="btn btn-sm btn-danger"
                                     onClick={() => handleDeleteUser(user)}
                                     title="Xóa"
                                   >
                                     <Trash2 size={16} />
                                   </button>
                                 </>
                               )}
                             </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Modal */}
      <UserModal
        user={selectedUser}
        isOpen={showUserModal}
        mode={modalMode}
        onClose={() => {
          setShowUserModal(false);
          setSelectedUser(null);
          setModalMode('view');
        }}
        onSave={handleSaveUser}
      />
    </div>
  );
};

export default UserManagement;
