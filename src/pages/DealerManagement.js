import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import DealerModal from '../components/modals/DealerModal';
import { dealerAPI } from '../services/api';
import '../styles/common.css';
import '../styles/filters.css';
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
    if (!status) return <span className="badge badge-secondary">N/A</span>;
    
    const statusLower = status.toLowerCase();
    const statusMap = {
      'active': { text: 'Hoạt động', class: 'badge-success' },
      'inactive': { text: 'Không hoạt động', class: 'badge-secondary' },
      'suspended': { text: 'Tạm dừng', class: 'badge-warning' },
      'terminated': { text: 'Chấm dứt', class: 'badge-danger' }
    };
    
    const statusInfo = statusMap[statusLower] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const getTypeBadge = (type) => {
    if (!type) return <span className="badge badge-secondary">N/A</span>;
    
    const typeLower = type.toLowerCase();
    const typeMap = {
      'authorized': { text: 'Đại lý ủy quyền', class: 'badge-primary' },
      'franchise': { text: 'Đại lý nhượng quyền', class: 'badge-info' },
      'partner': { text: 'Đối tác', class: 'badge-success' },
      'main_dealer': { text: 'Đại lý chính', class: 'badge-primary' },
      'sub_dealer': { text: 'Đại lý phụ', class: 'badge-info' },
      'service_center': { text: 'Trung tâm dịch vụ', class: 'badge-warning' },
      'showroom': { text: 'Showroom', class: 'badge-success' }
    };
    
    const typeInfo = typeMap[typeLower] || { text: type, class: 'badge-secondary' };
    return <span className={`badge ${typeInfo.class}`}>{typeInfo.text}</span>;
  };

  const handleView = async (dealer) => {
    try {
      // Load full dealer details
      const response = await dealerAPI.getDealer(dealer.dealerId);
      setSelectedDealer(response.data);
      setModalMode('view');
      setShowDealerModal(true);
    } catch (error) {
      console.error('Error loading dealer details:', error);
      toast.error('Không thể tải chi tiết đại lý');
    }
  };

  const handleEdit = async (dealer) => {
    try {
      // Load full dealer details
      const response = await dealerAPI.getDealer(dealer.dealerId);
      setSelectedDealer(response.data);
      setModalMode('edit');
      setShowDealerModal(true);
    } catch (error) {
      console.error('Error loading dealer details:', error);
      toast.error('Không thể tải chi tiết đại lý');
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
      render: (item) => <strong>{item?.dealerName || 'N/A'}</strong>
    },
    {
      key: 'dealerCode',
      label: 'Mã đại lý',
      render: (item) => <code>{item?.dealerCode || 'N/A'}</code>
    },
    {
      key: 'dealerType',
      label: 'Loại',
      render: (item) => getTypeBadge(item?.dealerType)
    },
    {
      key: 'city',
      label: 'Thành phố',
      render: (item) => item?.city || 'N/A'
    },
    {
      key: 'province',
      label: 'Tỉnh',
      render: (item) => item?.province || 'N/A'
    },
    {
      key: 'phone',
      label: 'Điện thoại',
      render: (item) => item?.phone || 'N/A'
    },
    {
      key: 'email',
      label: 'Email',
      render: (item) => item?.email || 'N/A'
    },
    {
      key: 'status',
      label: 'Trạng thái',
      render: (item) => getStatusBadge(item?.status)
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
          onView={handleView}
          onEdit={handleEdit}
          onDelete={handleDelete}
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