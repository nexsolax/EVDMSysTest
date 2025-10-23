import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { appointmentAPI, customerAPI, userAPI, vehicleAPI } from '../services/api';
import './AppointmentManagement.css';

const AppointmentManagement = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [customers, setCustomers] = useState([]);
  const [staff, setStaff] = useState([]);
  const [variants, setVariants] = useState([]);
  const [filterType, setFilterType] = useState('all');

  useEffect(() => {
    loadData();
    loadReferenceData();
  }, [filterType]);

  const loadData = async () => {
    try {
      setLoading(true);
      let response;
      
      switch (filterType) {
        case 'test-drives':
          response = await appointmentAPI.getTestDriveAppointments();
          break;
        case 'upcoming':
          response = await appointmentAPI.getUpcomingAppointments();
          break;
        default:
          response = await appointmentAPI.getAppointments();
      }
      
      setAppointments(response.data || []);
    } catch (error) {
      console.error('Error loading appointments:', error);
      toast.error('Không thể tải danh sách cuộc hẹn');
    } finally {
      setLoading(false);
    }
  };

  const loadReferenceData = async () => {
    try {
      const [customersRes, staffRes, variantsRes] = await Promise.all([
        customerAPI.getCustomers(),
        userAPI.getUsers(),
        vehicleAPI.getVariants()
      ]);
      
      setCustomers(customersRes.data || []);
      setStaff(staffRes.data || []);
      setVariants(variantsRes.data || []);
    } catch (error) {
      console.error('Error loading reference data:', error);
    }
  };

  const getCustomerName = (customerId) => {
    const customer = customers.find(c => c.customerId === customerId);
    return customer ? `${customer.firstName} ${customer.lastName}` : 'N/A';
  };

  const getStaffName = (staffId) => {
    const staffMember = staff.find(s => s.userId === staffId);
    return staffMember ? `${staffMember.firstName} ${staffMember.lastName}` : 'N/A';
  };

  const getVariantName = (variantId) => {
    const variant = variants.find(v => v.variantId === variantId);
    return variant ? variant.variantName : 'N/A';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const formatTime = (timeString) => {
    if (!timeString) return 'N/A';
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      'scheduled': { text: 'Đã lên lịch', class: 'badge-primary' },
      'confirmed': { text: 'Đã xác nhận', class: 'badge-success' },
      'in_progress': { text: 'Đang thực hiện', class: 'badge-warning' },
      'completed': { text: 'Hoàn thành', class: 'badge-success' },
      'cancelled': { text: 'Đã hủy', class: 'badge-danger' },
      'no_show': { text: 'Không đến', class: 'badge-secondary' }
    };
    
    const statusInfo = statusMap[status] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const getTypeBadge = (type) => {
    const typeMap = {
      'consultation': { text: 'Tư vấn', class: 'badge-info' },
      'test_drive': { text: 'Lái thử', class: 'badge-warning' },
      'delivery': { text: 'Giao xe', class: 'badge-success' },
      'maintenance': { text: 'Bảo trì', class: 'badge-primary' }
    };
    
    const typeInfo = typeMap[type] || { text: type, class: 'badge-secondary' };
    return <span className={`badge ${typeInfo.class}`}>{typeInfo.text}</span>;
  };

  const handleView = async (appointment) => {
    try {
      const response = await appointmentAPI.getAppointment(appointment.appointmentId);
      const appointmentData = response.data;
      
      // Show appointment details in a modal or alert
      const details = `
        Tiêu đề: ${appointmentData.title || 'N/A'}
        Loại: ${appointmentData.appointmentType || 'N/A'}
        Khách hàng: ${getCustomerName(appointmentData.customer?.customerId)}
        Nhân viên: ${getStaffName(appointmentData.staff?.userId)}
        Xe: ${getVariantName(appointmentData.variant?.variantId)}
        Ngày: ${formatDate(appointmentData.appointmentDate)}
        Giờ: ${formatTime(appointmentData.appointmentTime)}
        Trạng thái: ${appointmentData.status || 'N/A'}
        Ghi chú: ${appointmentData.notes || 'Không có'}
      `;
      
      alert(details);
    } catch (error) {
      console.error('Error loading appointment details:', error);
      toast.error('Không thể tải thông tin cuộc hẹn');
    }
  };

  const handleEdit = async (appointment) => {
    try {
      const response = await appointmentAPI.getAppointment(appointment.appointmentId);
      const appointmentData = response.data;
      
      // Open edit modal with appointmentData
      // For now, show alert with edit form data
      const editData = {
        title: appointmentData.title,
        appointmentType: appointmentData.appointmentType,
        customerId: appointmentData.customer?.customerId,
        staffId: appointmentData.staff?.userId,
        variantId: appointmentData.variant?.variantId,
        appointmentDate: appointmentData.appointmentDate,
        appointmentTime: appointmentData.appointmentTime,
        status: appointmentData.status,
        notes: appointmentData.notes
      };
      
      console.log('Edit appointment data:', editData);
      toast.info('Chức năng chỉnh sửa sẽ được implement trong modal');
    } catch (error) {
      console.error('Error loading appointment for edit:', error);
      toast.error('Không thể tải thông tin cuộc hẹn');
    }
  };

  const handleDelete = async (appointment) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa cuộc hẹn "${appointment.title}"?`)) {
      try {
        await appointmentAPI.deleteAppointment(appointment.appointmentId);
        toast.success('Xóa cuộc hẹn thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting appointment:', error);
        toast.error('Không thể xóa cuộc hẹn');
      }
    }
  };

  const handleUpdateStatus = async (appointment, newStatus) => {
    try {
      await appointmentAPI.updateAppointmentStatus(appointment.appointmentId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating appointment status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const columns = [
    {
      key: 'title',
      label: 'Tiêu đề',
      render: (value) => <strong>{value || 'N/A'}</strong>
    },
    {
      key: 'appointmentType',
      label: 'Loại',
      render: (value) => getTypeBadge(value)
    },
    {
      key: 'customer',
      label: 'Khách hàng',
      render: (value, row) => getCustomerName(row.customer?.customerId)
    },
    {
      key: 'staff',
      label: 'Nhân viên',
      render: (value, row) => getStaffName(row.staff?.userId)
    },
    {
      key: 'variant',
      label: 'Xe',
      render: (value, row) => getVariantName(row.variant?.variantId)
    },
    {
      key: 'appointmentDate',
      label: 'Ngày',
      render: (value) => formatDate(value)
    },
    {
      key: 'appointmentTime',
      label: 'Giờ',
      render: (value) => formatTime(value)
    },
    {
      key: 'status',
      label: 'Trạng thái',
      render: (value) => getStatusBadge(value)
    }
  ];

  const actions = [
    {
      label: 'Xem',
      className: 'btn-info',
      onClick: handleView
    },
    {
      label: 'Sửa',
      className: 'btn-warning',
      onClick: handleEdit
    },
    {
      label: 'Xóa',
      className: 'btn-danger',
      onClick: handleDelete
    }
  ];

  const statusActions = [
    {
      label: 'Xác nhận',
      value: 'confirmed',
      className: 'btn-success'
    },
    {
      label: 'Hoàn thành',
      value: 'completed',
      className: 'btn-primary'
    },
    {
      label: 'Hủy',
      value: 'cancelled',
      className: 'btn-danger'
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="appointment-management">
      <div className="page-header">
        <h1>Quản lý cuộc hẹn</h1>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => toast.info('Chức năng tạo cuộc hẹn mới sẽ được implement')}
          >
            <i className="fas fa-plus"></i> Tạo cuộc hẹn mới
          </button>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-tabs">
          <button 
            className={`filter-tab ${filterType === 'all' ? 'active' : ''}`}
            onClick={() => setFilterType('all')}
          >
            Tất cả
          </button>
          <button 
            className={`filter-tab ${filterType === 'test-drives' ? 'active' : ''}`}
            onClick={() => setFilterType('test-drives')}
          >
            Lái thử
          </button>
          <button 
            className={`filter-tab ${filterType === 'upcoming' ? 'active' : ''}`}
            onClick={() => setFilterType('upcoming')}
          >
            Sắp tới
          </button>
        </div>
      </div>

      <div className="data-section">
        <DataTable
          data={appointments}
          columns={columns}
          actions={actions}
          statusActions={statusActions}
          onStatusUpdate={handleUpdateStatus}
          searchable={true}
          searchPlaceholder="Tìm kiếm cuộc hẹn..."
        />
      </div>
    </div>
  );
};

export default AppointmentManagement;