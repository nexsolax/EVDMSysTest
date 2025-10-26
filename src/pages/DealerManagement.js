import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import DealerModal from '../components/modals/DealerModal';
import { dealerAPI } from '../services/api';
import './DealerManagement.css';

const DealerManagement = () => {
  const [dealers, setDealers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('all');
  const [selectedDealer, setSelectedDealer] = useState(null);
  const [showDealerModal, setShowDealerModal] = useState(false);
  const [modalMode, setModalMode] = useState('view');

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

  const handleView = (dealer) => {
    setSelectedDealer(dealer);
    setModalMode('view');
    setShowDealerModal(true);
  };

  const handleEdit = (dealer) => {
    setSelectedDealer(dealer);
    setModalMode('edit');
    setShowDealerModal(true);
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

  const handleCreate = () => {
    setSelectedDealer(null);
    setModalMode('create');
    setShowDealerModal(true);
  };

  const handleSaveDealer = async (dealerId, dealerData) => {
    try {
      if (dealerId) {
        // Update existing dealer
        await dealerAPI.updateDealer(dealerId, dealerData);
        toast.success('Cập nhật đại lý thành công');
      } else {
        // Create new dealer
        await dealerAPI.createDealer(dealerData);
        toast.success('Tạo đại lý thành công');
      }
      loadData();
      setShowDealerModal(false);
    } catch (error) {
      console.error('Error saving dealer:', error);
      throw error; // Re-throw to be handled by modal
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
            onClick={handleCreate}
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
          searchable={true}
          searchPlaceholder="Tìm kiếm đại lý..."
        />
      </div>

      {/* Dealer Modal */}
      <DealerModal
        dealer={selectedDealer}
        isOpen={showDealerModal}
        mode={modalMode}
        onClose={() => {
          setShowDealerModal(false);
          setSelectedDealer(null);
          setModalMode('view');
        }}
        onSave={handleSaveDealer}
      />
    </div>
  );
};

export default DealerManagement;