import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { dealerAPI } from '../services/api';
import './DealerManagement.css';

const DealerManagement = () => {
  const [dealers, setDealers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('all');

  useEffect(() => {
    loadData();
  }, [filterStatus]);

  const loadData = async () => {
    try {
      setLoading(true);
      let response;
      
      switch (filterStatus) {
        case 'active':
          response = await dealerAPI.getActiveDealers();
          break;
        default:
          response = await dealerAPI.getDealers();
      }
      
      setDealers(response.data || []);
    } catch (error) {
      console.error('Error loading dealers:', error);
      toast.error('Không thể tải danh sách đại lý');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      'active': { text: 'Hoạt động', class: 'badge-success' },
      'inactive': { text: 'Không hoạt động', class: 'badge-secondary' },
      'suspended': { text: 'Tạm dừng', class: 'badge-warning' },
      'terminated': { text: 'Chấm dứt', class: 'badge-danger' }
    };
    
    const statusInfo = statusMap[status] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const getTypeBadge = (type) => {
    const typeMap = {
      'main_dealer': { text: 'Đại lý chính', class: 'badge-primary' },
      'sub_dealer': { text: 'Đại lý phụ', class: 'badge-info' },
      'service_center': { text: 'Trung tâm dịch vụ', class: 'badge-warning' },
      'showroom': { text: 'Showroom', class: 'badge-success' }
    };
    
    const typeInfo = typeMap[type] || { text: type, class: 'badge-secondary' };
    return <span className={`badge ${typeInfo.class}`}>{typeInfo.text}</span>;
  };

  const handleView = async (dealer) => {
    try {
      const response = await dealerAPI.getDealer(dealer.dealerId);
      const dealerData = response.data;
      
      // Show dealer details in a modal or alert
      const details = `
        Tên đại lý: ${dealerData.dealerName || 'N/A'}
        Mã đại lý: ${dealerData.dealerCode || 'N/A'}
        Loại: ${dealerData.dealerType || 'N/A'}
        Địa chỉ: ${dealerData.address || 'N/A'}
        Thành phố: ${dealerData.city || 'N/A'}
        Tỉnh: ${dealerData.province || 'N/A'}
        Mã bưu điện: ${dealerData.postalCode || 'N/A'}
        Điện thoại: ${dealerData.phone || 'N/A'}
        Email: ${dealerData.email || 'N/A'}
        Website: ${dealerData.website || 'N/A'}
        Ngày thành lập: ${formatDate(dealerData.establishedDate)}
        Trạng thái: ${dealerData.status || 'N/A'}
        Mô tả: ${dealerData.description || 'Không có'}
      `;
      
      alert(details);
    } catch (error) {
      console.error('Error loading dealer details:', error);
      toast.error('Không thể tải thông tin đại lý');
    }
  };

  const handleEdit = async (dealer) => {
    try {
      const response = await dealerAPI.getDealer(dealer.dealerId);
      const dealerData = response.data;
      
      // Open edit modal with dealerData
      const editData = {
        dealerName: dealerData.dealerName,
        dealerCode: dealerData.dealerCode,
        dealerType: dealerData.dealerType,
        address: dealerData.address,
        city: dealerData.city,
        province: dealerData.province,
        postalCode: dealerData.postalCode,
        phone: dealerData.phone,
        email: dealerData.email,
        website: dealerData.website,
        establishedDate: dealerData.establishedDate,
        status: dealerData.status,
        description: dealerData.description
      };
      
      console.log('Edit dealer data:', editData);
      toast.info('Chức năng chỉnh sửa sẽ được implement trong modal');
    } catch (error) {
      console.error('Error loading dealer for edit:', error);
      toast.error('Không thể tải thông tin đại lý');
    }
  };

  const handleDelete = async (dealer) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa đại lý "${dealer.dealerName}"?`)) {
      try {
        await dealerAPI.deleteDealer(dealer.dealerId);
        toast.success('Xóa đại lý thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting dealer:', error);
        toast.error('Không thể xóa đại lý');
      }
    }
  };

  const handleUpdateStatus = async (dealer, newStatus) => {
    try {
      if (newStatus === 'active') {
        await dealerAPI.activateDealer(dealer.dealerId);
      } else if (newStatus === 'inactive') {
        await dealerAPI.deactivateDealer(dealer.dealerId);
      }
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating dealer status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const columns = [
    {
      key: 'dealerName',
      label: 'Tên đại lý',
      render: (value) => <strong>{value || 'N/A'}</strong>
    },
    {
      key: 'dealerCode',
      label: 'Mã đại lý',
      render: (value) => <code>{value || 'N/A'}</code>
    },
    {
      key: 'dealerType',
      label: 'Loại',
      render: (value) => getTypeBadge(value)
    },
    {
      key: 'city',
      label: 'Thành phố',
      render: (value) => value || 'N/A'
    },
    {
      key: 'province',
      label: 'Tỉnh',
      render: (value) => value || 'N/A'
    },
    {
      key: 'phone',
      label: 'Điện thoại',
      render: (value) => value || 'N/A'
    },
    {
      key: 'email',
      label: 'Email',
      render: (value) => value || 'N/A'
    },
    {
      key: 'establishedDate',
      label: 'Ngày thành lập',
      render: (value) => formatDate(value)
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
      label: 'Kích hoạt',
      value: 'active',
      className: 'btn-success'
    },
    {
      label: 'Vô hiệu hóa',
      value: 'inactive',
      className: 'btn-secondary'
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-management">
      <div className="page-header">
        <h1>Quản lý đại lý</h1>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => toast.info('Chức năng tạo đại lý mới sẽ được implement')}
          >
            <i className="fas fa-plus"></i> Tạo đại lý mới
          </button>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-tabs">
          <button 
            className={`filter-tab ${filterStatus === 'all' ? 'active' : ''}`}
            onClick={() => setFilterStatus('all')}
          >
            Tất cả
          </button>
          <button 
            className={`filter-tab ${filterStatus === 'active' ? 'active' : ''}`}
            onClick={() => setFilterStatus('active')}
          >
            Đang hoạt động
          </button>
        </div>
      </div>

      <div className="data-section">
        <DataTable
          data={dealers}
          columns={columns}
          actions={actions}
          statusActions={statusActions}
          onStatusUpdate={handleUpdateStatus}
          searchable={true}
          searchPlaceholder="Tìm kiếm đại lý..."
        />
      </div>
    </div>
  );
};

export default DealerManagement;